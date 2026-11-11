package com.meuteclado

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object AiClient {

    private val http = OkHttpClient.Builder()
        .callTimeout(60, TimeUnit.SECONDS)
        .build()

    /** Envia o print (JPEG em base64) e o tom ao proxy; devolve as opções de resposta. */
    fun replies(imageB64: String, tone: String): List<String> {
        val body = JSONObject()
            .put("image", imageB64)
            .put("tone", tone)
            .toString()
            .toRequestBody("application/json".toMediaType())

        val req = Request.Builder().url(BuildConfig.PROXY_URL).post(body).build()
        http.newCall(req).execute().use { res ->
            if (!res.isSuccessful) throw IOException("HTTP ${res.code}")
            val arr = JSONArray(res.body!!.string())
            return List(arr.length()) { arr.getString(it) }
        }
    }
}
