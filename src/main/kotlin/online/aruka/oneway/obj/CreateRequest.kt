package online.aruka.oneway.obj

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CreateRequest(
    val name: String, // version_name
    @SerialName("version_number") val versionNumber: String,
    val changelog: String?,
    val dependencies: List<Dependency>,
    @SerialName("game_versions") val gameVersions: List<String>,
    @SerialName("version_type") val versionType: String,
    val loaders: List<String>,
    val featured: Boolean,
    val status: String,
    @SerialName("project_id") val projectId: String,
    @SerialName("file_parts") val fileParts: List<String>,
    @SerialName("primary_file") val primaryFileName: String = PRIMARY_FILE_FIELD_NAME,
    @SerialName("requested_status") val requestedStatus: String? = null,
) {
    companion object {
        const val PRIMARY_FILE_FIELD_NAME = "primary_file"
    }

    @Serializable
    data class Dependency(
        @SerialName("version_id") val versionId: String?,
        @SerialName("project_id") val projectId: String?,
        @SerialName("file_name") val fileName: String?,
        @SerialName("dependency_type") val type: String
    ) {
        companion object {
            private val elementRegex = "([vpft])=(.*)".toRegex()
            val types: Set<String> = setOf("required", "optional", "incompatible", "embedded")

            fun fromDependencyIsh(dep: String): Result<Dependency> {
                // v=VERSION_ID/p=PROJECT_ID/f=FILE_NAME(external file name)/t=DEPENDENCY_TYPE
                // vt, pt, vpt, ft
                val split = dep.split("/")
                val values: MutableMap<String, String> = mutableMapOf()
                for (entry: String in split) {
                    if (!entry.matches(elementRegex)) {
                        continue
                    }

                    val matched: MatchResult = elementRegex.find(entry) ?: continue
                    val type: String = matched.groupValues[1]
                    val value: String = matched.groupValues[2]
                    values[type] = value
                }

                val type: String = values["t"]
                    ?: return Result.failure(IllegalArgumentException("Dependency must contains type."))

                val result = Dependency(
                    versionId = values["v"], projectId = values["p"], fileName = values["f"], type = type)

                result.isValid().onFailure { return Result.failure(it) }
                return Result.success(result)
            }
        }

        fun isValid(): Result<Unit> {
            if (this.type !in types) {
                return Result.failure(IllegalStateException("Dependency type is not valid."))
            }

            // valid types
            // v, p, vp, f
            if (this.fileName != null) {
                if (this.versionId == null && this.projectId == null) {
                    return Result.success(Unit)
                }
                return Result.failure(IllegalStateException("Dependency allowed vt, pt, vpt, ft."))
            }

            if (this.versionId.isNullOrBlank() && this.projectId.isNullOrBlank()) {
                return Result.failure(IllegalStateException("Dependency allowed vt, pt, vpt, ft."))
            }

            return Result.success(Unit)
        }
    }
}
