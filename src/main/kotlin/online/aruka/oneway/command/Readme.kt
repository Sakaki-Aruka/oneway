package online.aruka.oneway.command

import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import online.aruka.oneway.obj.UpdateProjectRequest
import online.aruka.oneway.util.DefaultSettings
import picocli.CommandLine
import java.io.File
import java.util.concurrent.Callable

@CommandLine.Command(name = "readme", description = ["update project readme"])
class Readme : Callable<Int> {
    @CommandLine.Parameters(index = "0", description = ["README file path"])
    lateinit var filePath: File

    @CommandLine.Option(names = ["--token"], required = true)
    lateinit var token: String

    @CommandLine.Option(names = ["-p", "--project-id"], required = true)
    lateinit var projectId: String

    @CommandLine.Option(names = ["-e", "--show-error"], required = false)
    var showError: Boolean = false

    @CommandLine.Option(names = ["--show-response"], required = false)
    var showResponse: Boolean = false

    @CommandLine.Option(names = ["--test"], required = false)
    var isTest: Boolean = false

    private fun endpoint(): String {
        return (if (this.isTest) DefaultSettings.TEST_ENDPOINT_BASE else DefaultSettings.ENDPOINT_BASE) + "project/${projectId}"
    }

    override fun call(): Int {
        val errors: MutableList<String> = mutableListOf()

        if (this.token.isBlank()) {
            errors.add("Token is blank.")
        }

        if (!this.filePath.exists()) {
            errors.add("File not found: ${this.filePath.path}")
        }

        if (errors.isNotEmpty()) {
            if (this.showError) {
                System.err.println(errors.joinToString(System.lineSeparator()))
            }
            return 3
        }

        val content: String = this.filePath.readLines().joinToString(System.lineSeparator())
        val requestBody = Json.encodeToString(UpdateProjectRequest(body = content))
            .toRequestBody("application/json".toMediaType())

        val httpRequest = Request.Builder()
            .url(endpoint())
            .patch(requestBody)
            .addHeader("Authorization", token)
            .build()

        val client = OkHttpClient()
        runCatching { client.newCall(httpRequest).execute() }
            .onFailure {
                if (this.showError) {
                    System.err.println("Failed to send request.")
                    if (this.isTest) {
                        it.message?.let { errorMessage -> System.err.println(errorMessage) }
                    }
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

        if (this.showError) {
            System.err.println("How Did We Get Here?")
        }
        return 4
    }
}
