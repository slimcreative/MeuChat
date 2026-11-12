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

    /** Manda o print + tema + tom direto para a API da Anthropic e devolve as opções. */
    fun replies(apiKey: String, imageB64: String, topic: String, tone: String): List<String> {
        val image = JSONObject()
            .put("type", "image")
            .put("source", JSONObject()
                .put("type", "base64")
                .put("media_type", "image/jpeg")
                .put("data", imageB64))
        val text = JSONObject()
            .put("type", "text")
            .put("text", Prompts.build(topic, tone))

        val payload = JSONObject()
            .put("model", "claude-haiku-5-5")
            .put("max_tokens", 500)
            .put("messages", JSONArray().put(
                JSONObject()
                    .put("role", "user")
                    .put("content", JSONArray().put(image).put(text))
            ))
            .toString()

        val req = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .post(payload.toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(req).execute().use { res ->
            val body = res.body?.string().orEmpty()
            if (!res.isSuccessful) throw IOException("API ${res.code}: ${body.take(200)}")

            val content = JSONObject(body).getJSONArray("content")
            var reply: String? = null
            for (i in 0 until content.length()) {
                val block = content.getJSONObject(i)
                if (block.optString("type") == "text") reply = block.getString("text")
            }
            if (reply == null) throw IOException("resposta sem texto")
            val json = JSONObject(reply.substring(reply.indexOf('{'), reply.lastIndexOf('}') + 1))
            val arr = json.getJSONArray("options")
            return List(arr.length()) { arr.getString(it) }
        }
    }
}
