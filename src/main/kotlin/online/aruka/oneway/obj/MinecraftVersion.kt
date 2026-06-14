package online.aruka.oneway.obj

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import online.aruka.oneway.util.ApiCaller
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class MinecraftVersion(
    val version: String,
    val versionType: String,
    val releaseAt: ZonedDateTime,
    val isMajorVersion: Boolean
) {
    @Serializable
    data class ForParse(
        val version: String,
        @SerialName("version_type") val versionType: String,
        @SerialName("date") val releaseAt: String,
        @SerialName("major") val isMajorVersion: Boolean
    ) {
        fun toFormal(): MinecraftVersion {
            return MinecraftVersion(
                version,
                versionType,
                ZonedDateTime.parse(releaseAt, DateTimeFormatter.ISO_OFFSET_DATE_TIME),
                isMajorVersion
            )
        }
    }

    companion object {
        private const val endpoint = "https://api.modrinth.com/v2/tag/game_version"

        fun getAllVersions(): List<MinecraftVersion> {
            val response = ApiCaller.get(endpoint)
            val body: String = response.getOrNull()?.let { (_, b) -> b }
                ?: return emptyList()
            return getFromListResponse(body)
        }

        fun getRange(left: String, right: String): List<MinecraftVersion> {
            val all: List<MinecraftVersion> = getAllVersions()
            if (all.isEmpty()) {
                return emptyList()
            }
            val leftIndex: Int = all.indexOfFirst { it.version == left }
            val rightIndex: Int = all.indexOfFirst { it.version == right }
            if (leftIndex == -1 && rightIndex == -1) {
                return emptyList()
            }

            val startIndex: Int = minOf(leftIndex, rightIndex, 0)
            val endIndex: Int = maxOf(leftIndex, rightIndex, all.lastIndex) + 1
            return all.subList(startIndex, endIndex)
        }

        fun getFromListResponse(json: String): List<MinecraftVersion> {
            if (json.isEmpty()) {
                return emptyList()
            }

            /*
             * example
               {
                "version": "rd-132211",
                "version_type": "alpha",
                "date": "2009-05-13T20:11:00Z",
                "major": false
              }

             */

            val informalList: List<ForParse> = Json.decodeFromString(json)
            return informalList
                .filter { it.versionType == "release" }
                .map { it.toFormal() }
        }

        fun getFromVersionIshSet(vararg vs: String): Pair<Set<MinecraftVersion>, Set<String>> {
            val result: MutableSet<MinecraftVersion> = mutableSetOf()
            val invalid: MutableSet<String> = mutableSetOf()
            val allVersions: List<MinecraftVersion> = getAllVersions()
            for (versionIsh: String in vs) {
                val split: List<String> = versionIsh.split("~")
                when (split.size) {
                    1 -> allVersions
                        .firstOrNull { v -> v.version == versionIsh }
                        ?.let { v -> result.add(v) }
                        ?: invalid.add(versionIsh)
                    2 -> getRange(split.first(), split.last())
                        .takeUnless { it.isEmpty() }
                        ?.let { range -> result.addAll(range) }
                        ?: invalid.add(versionIsh)
                    else -> invalid.add(versionIsh)
                }
            }
            return result to invalid
        }
    }
}