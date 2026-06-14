package online.aruka.oneway.util

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request

object ApiCaller {
    val OCTET_STREAM = "application/octet-stream".toMediaType()

    fun get(endpoint: String): Result<Pair<Int, String>> {
        val client = OkHttpClient()
        val request: Request = Request.Builder()
            .url(endpoint)
            .get()
            .build()

        val result: Pair<Int, String>
        try {
            client.newCall(request).execute().use {
                result = it.code to it.body.string()
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }

        return Result.success(result)
    }
}