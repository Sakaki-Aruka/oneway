package online.aruka.oneway.command

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import online.aruka.oneway.obj.parshal.VersionNumber
import online.aruka.oneway.util.DefaultSettings
import picocli.CommandLine
import java.util.concurrent.Callable

@CommandLine.Command(name = "not-exists", description = ["check version conflict"])
class VersionCheck : Callable<Int> {
    /*
     * need arguments
     * - project name
     * - version string
     */
    @CommandLine.Parameters(index = "0", description = ["project name"])
    lateinit var projectName: String

    @CommandLine.Parameters(index = "1", description = ["check version"])
    lateinit var version: String

    @CommandLine.Option(names = ["-e", "--show-error"])
    var showError: Boolean = false

    override fun call(): Int {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(DefaultSettings.ENDPOINT_BASE + "project/${projectName}/version")
            .get()
            .build()

        val response: Response = client.newCall(request).execute()
        if (!response.isSuccessful) {
            if (this.showError) {
                System.err.println("Invalid response: ${response.code}, ${response.message}")
            }
            return 3
        }

        var versions: List<VersionNumber>
        try {
            versions = VersionNumber.getFromListResponse(response.body.string())
        } catch (e: Exception) {
            if (this.showError) {
                System.err.println("Response parse error. ${e.message ?: ""}")
            }
            return 4
        }

        if (versions.any { it.version == this.version }) {
            if (this.showError) {
                System.err.println("${this.version} has already released.")
            }
            return 2
        }

        return 0
    }
}