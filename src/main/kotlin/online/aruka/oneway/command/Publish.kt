package online.aruka.oneway.command

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import online.aruka.oneway.obj.CreateRequest
import online.aruka.oneway.obj.Loader
import online.aruka.oneway.obj.MinecraftVersion
import online.aruka.oneway.obj.ReleaseVersionType
import online.aruka.oneway.util.ApiCaller
import online.aruka.oneway.util.DefaultSettings
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(name = "version", description = ["publish version with specified settings"])
class Publish : Callable<Int> {
    @CommandLine.Option(names = ["--token"], required = true)
    lateinit var token: String

    @CommandLine.Option(names = ["-f", "--file"], required = true)
    lateinit var files: Array<File>

    @CommandLine.Option(names = ["-V"], required = true)
    lateinit var version: String

    @CommandLine.Option(names = ["--version_name"], required = false)
    var versionName: String? = null

    @CommandLine.Option(names = ["--changelog"], required = false)
    var changelogFile: File? = null

    @CommandLine.Option(
        names = ["-d", "--dependency"],
        required = false,
        description = [
            "Dependency. Format: v=<version_id>/p=<project_id>/f=<file_name>/t=<type>",
            "t (type) is required. Valid types: required, optional, incompatible, embedded",
            "f (file_name) is an external file name, not a local path.",
            "If version_id is specified, type is the only other required field.",
            "Valid combinations: v+t, p+t, v+p+t, f+t",
            "Example (custom-crafter-api 5.2.1): -d v=MHzOV8q4/t=required",
            "Repeatable."
        ]
    )
    var dependencies: Array<String>? = null

    @CommandLine.Option(names = ["-g", "--game_version"], split = ",", required = true)
    lateinit var gameVersions: Array<String>

    @CommandLine.Option(names = ["-t", "--release-type"], required = true, description = ["Release type: release, beta, alpha"])
    lateinit var releaseType: String

    @CommandLine.Option(names = ["-l", "--platform"], split = ",", required = true)
    lateinit var platforms: Array<String>

    @CommandLine.Option(names = ["--featured"], required = false)
    var featured: Boolean = false

    @CommandLine.Option(names = ["-p", "--project_id"], required = true)
    lateinit var projectId: String

    @CommandLine.Option(names = ["-s", "--status"], required = false, description = ["Status: listed, draft, unlisted,  archived"])
    var status: String = "draft"

    @CommandLine.Option(names = ["-e", "--show-error"], required = false)
    var showError: Boolean = false

    @CommandLine.Option(names = ["--show-response"], required = false)
    var showResponse: Boolean = false

    @CommandLine.Option(names = ["--test"], required = false)
    var isTest: Boolean = false

    private fun endpoint(): String {
        return (if (this.isTest) DefaultSettings.ENDPOINT_BASE else DefaultSettings.TEST_ENDPOINT_BASE) + "version"
    }

    override fun call(): Int {
        val changelogContent: String? = changelogFile?.let { file ->
            file.readLines().joinToString(System.lineSeparator())
        }

        val (versions, invalidVersions) = MinecraftVersion.getFromVersionIshSet(*this.gameVersions)
        if (invalidVersions.isNotEmpty()) {
            if (this.showError) {
                System.err.println("Invalid game versions: ${invalidVersions.joinToString(", ")}")
            }
            return 3
        }

        if (versions.isEmpty()) {
            if (this.showError) {
                System.err.println("Valid game versions not exist.")
            }
            return 3
        }

        if (this.releaseType !in ReleaseVersionType.entries.map { it.type }) {
            if (this.showError) {
                System.err.println("Invalid release type: $releaseType")
            }
            return 3
        }

        val invalidLoaders: List<String> = this.platforms
            .filter { platform -> Loader.entries.none { it.v == platform } }
        if (invalidLoaders.isNotEmpty()) {
            if (this.showError) {
                System.err.println("Invalid loaders: ${invalidLoaders.joinToString(", ")}")
            }
            return 3
        }

        val dependencyWarnings: MutableList<Throwable> = mutableListOf()
        val validDependencies: MutableList<CreateRequest.Dependency> = mutableListOf()
        this.dependencies?.let { deps ->
            deps.forEach { entry ->
                CreateRequest.Dependency.fromDependencyIsh(entry)
                    .onFailure { dependencyWarnings.add(it) }
                    .onSuccess { validDependencies.add(it) }
            }
        }
        if (dependencyWarnings.isNotEmpty()) {
            if (this.showError) {
                val msg: String = dependencyWarnings.joinToString(System.lineSeparator()) { it.message ?: "" }
                System.err.println("Invalid dependency. ${System.lineSeparator()}$msg")
            }
            return 3
        }

        if (this.status !in setOf("listed", "draft", "unlisted", "archived")) {
            if (this.showError) {
                System.err.println("Invalid status. (Set listed, draft, unlisted or archived)")
            }
            return 3
        }

        if (this.files.isEmpty()) {
            if (this.showError) {
                System.err.println("Files and a primary file not specified.")
            }
            return 3
        }

        val fileNameCounts: Map<String, Int> = this.files
            .groupingBy { it.name }
            .eachCount()

        if (fileNameCounts.any { (_, times) -> times > 1 }) {
            if (this.showError) {
                val names: String = fileNameCounts
                    .filter { (_, times) -> times > 1 }
                    .map { (name, _) -> name }
                    .joinToString(", ")
                System.err.println("File name duplicated: $names")
            }
            return 3
        }

        val createRequest = CreateRequest(
            name = versionName ?: version,
            versionNumber = version,
            changelog = changelogContent,
            dependencies = validDependencies,
            gameVersions = versions.map { it.version },
            versionType = releaseType,
            loaders = platforms.toList(),
            featured = featured,
            status = status,
            projectId = projectId,
            fileParts = listOf(CreateRequest.PRIMARY_FILE_FIELD_NAME) + files.map { it.name }.drop(1),
        )

        val multipartBodyBuilder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                name = "data",
                filename = null,
                body = Json.encodeToString(createRequest).toRequestBody("application/json".toMediaType())
            )

        multipartBodyBuilder.addFormDataPart(
            name = CreateRequest.PRIMARY_FILE_FIELD_NAME,
            filename = files.first().name,
            body = files.first().asRequestBody(ApiCaller.OCTET_STREAM)
        )

        this.files.drop(1).forEach { f ->
            multipartBodyBuilder.addFormDataPart(
                name = f.name,
                filename = f.name,
                body = f.asRequestBody(ApiCaller.OCTET_STREAM))
        }

        val httpRequest = Request.Builder()
            .url(endpoint() + "version")
            .post(multipartBodyBuilder.build())
            .addHeader("Authorization", token)
            .addHeader("User-Agent", "${createRequest.projectId},${createRequest.versionNumber}")
            .build()

        val client = OkHttpClient()
        runCatching { client.newCall(httpRequest).execute() }
            .onFailure {
                if (this.showError) {
                    System.err.println("Failed to send request.")
                }
                return 4
            }
            .onSuccess { resp ->
                if (this.showError && !resp.isSuccessful) {
                    System.err.println("Failed to request. (Code: ${resp.code})")
                }
                if (this.showResponse) {
                    println("Response: ${resp.body.string()}")
                }

                return if (resp.isSuccessful) 0 else 4
            }

        return 4
    }
}