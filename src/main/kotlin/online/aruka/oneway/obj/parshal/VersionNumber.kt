package online.aruka.oneway.obj.parshal

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

data class VersionNumber(val version: String) {
    companion object {
        fun getFromListResponse(json: String): List<VersionNumber> {
            if (json.isEmpty()) {
                return emptyList()
            }
            val result: MutableList<VersionNumber> = mutableListOf()
            val element: JsonElement = Json.parseToJsonElement(json)
            for (entry in element.jsonArray) {
                entry.jsonObject["version_number"]?.let { num ->
                    num.jsonPrimitive.contentOrNull?.let { primitive ->
                        result.add(VersionNumber(primitive))
                    }
                }
            }
            return result
        }
    }
}
