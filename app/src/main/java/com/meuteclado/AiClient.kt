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

    /** Sugestões prontas + leitura opcional do que a pessoa quis dizer. */
    class Suggestions(val options: List<String>, val reading: String)

    /** A API respondeu 200, mas sem conteúdo aproveitável (recusa, corte, vazio). */
    private class EmptyReply(val reason: String) : IOException(reason)

    private class HttpError(val code: Int, val detail: String) : IOException("API $code")

    private const val MODEL = "claude-haiku-5-5"
    private const val FALLBACK_MODEL = "claude-sonnet-5-5"
    private const val TOOL = "sugerir_respostas"

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .callTimeout(90, TimeUnit.SECONDS)
        .build()

    /**
     * Manda o print + tema + tom para a API e devolve as opções.
     * Ordem das tentativas:
     *  1) modelo rápido, saída estruturada (ferramenta forçada);
     *  2) se a API rejeitar o formato (400), mesmo modelo em modo texto/JSON;
     *  3) se vier vazio ou recusado, modelo reserva.
     */
    fun replies(apiKey: String, imageB64: String, topic: String, tone: String): Suggestions {
        try {
            return try {
                call(apiKey, MODEL, imageB64, topic, tone, useTool = true)
            } catch (e: HttpError) {
                if (e.code != 400) throw e
                call(apiKey, MODEL, imageB64, topic, tone, useTool = false)
            }
        } catch (first: EmptyReply) {
            try {
                return call(apiKey, FALLBACK_MODEL, imageB64, topic, tone, useTool = true)
            } catch (second: HttpError) {
                throw IOException(friendly(second))
            } catch (second: EmptyReply) {
                throw IOException("A IA não gerou resposta para esta tela (${first.reason}). Toque em ↻ para tentar de novo.")
            }
        } catch (e: HttpError) {
            throw IOException(friendly(e))
        }
    }

    private fun friendly(e: HttpError): String = when (e.code) {
        401, 403 -> "Chave da API inválida. Abra o app e salve a chave de novo."
        429 -> "Limite da API atingido. Espere um pouco e toque em ↻."
        in 500..599 -> "A API está instável agora (${e.code}). Toque em ↻."
        else -> "Erro ${e.code}: ${e.detail}"
    }

    private fun call(
        apiKey: String, model: String, imageB64: String,
        topic: String, tone: String, useTool: Boolean
    ): Suggestions {
        val image = JSONObject()
            .put("type", "image")
            .put("source", JSONObject()
                .put("type", "base64")
                .put("media_type", "image/jpeg")
                .put("data", imageB64))
        val text = JSONObject()
            .put("type", "text")
            .put("text", Prompts.build(topic, tone, jsonMode = !useTool))

        val payload = JSONObject()
            .put("model", model)
            .put("max_tokens", 1024)
            .put("system", Prompts.SYSTEM)
            .put("messages", JSONArray().put(
                JSONObject()
                    .put("role", "user")
                    .put("content", JSONArray().put(image).put(text))
            ))
        if (useTool) {
            payload.put("tools", JSONArray().put(toolSpec()))
            payload.put("tool_choice", JSONObject().put("type", "tool").put("name", TOOL))
        }

        val req = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .header("x-api-key", apiKey)
            .header("anthropic-version", "2023-06-01")
            .post(payload.toString().toRequestBody("application/json".toMediaType()))
            .build()

        http.newCall(req).execute().use { res ->
            val body = res.body?.string().orEmpty()
            if (!res.isSuccessful) throw HttpError(res.code, errorMessage(body))

            val root = try {
                JSONObject(body)
            } catch (e: Exception) {
                throw EmptyReply("resposta ilegível")
            }
            val content = root.optJSONArray("content") ?: JSONArray()
            val stop = root.optString("stop_reason", "desconhecido")

            // 1) saída estruturada
            for (i in 0 until content.length()) {
                val block = content.optJSONObject(i) ?: continue
                if (block.optString("type") == "tool_use") {
                    parse(block.optJSONObject("input"))?.let { return it }
                }
            }
            // 2) texto (junta todos os blocos de texto) contendo JSON
            val sb = StringBuilder()
            for (i in 0 until content.length()) {
                val block = content.optJSONObject(i) ?: continue
                if (block.optString("type") == "text") sb.append(block.optString("text"))
            }
            val txt = sb.toString()
            val a = txt.indexOf('{')
            val b = txt.lastIndexOf('}')
            if (a in 0 until b) {
                val obj = try { JSONObject(txt.substring(a, b + 1)) } catch (e: Exception) { null }
                parse(obj)?.let { return it }
            }

            throw EmptyReply(when (stop) {
                "refusal" -> "a IA recusou esta tela"
                "max_tokens" -> "resposta cortada"
                else -> "resposta vazia, motivo: $stop"
            })
        }
    }

    private fun parse(obj: JSONObject?): Suggestions? {
        val arr = obj?.optJSONArray("options") ?: return null
        val list = ArrayList<String>()
        for (i in 0 until arr.length()) {
            val s = arr.optString(i).trim()
            if (s.isNotEmpty()) list.add(s)
        }
        if (list.isEmpty()) return null
        return Suggestions(list, obj.optString("leitura").trim())
    }

    private fun errorMessage(body: String): String = try {
        JSONObject(body).getJSONObject("error").getString("message").take(160)
    } catch (e: Exception) {
        body.take(160)
    }

    private fun toolSpec(): JSONObject = JSONObject()
        .put("name", TOOL)
        .put("description", "Entrega as sugestões de mensagem prontas para o usuário enviar.")
        .put("input_schema", JSONObject()
            .put("type", "object")
            .put("properties", JSONObject()
                .put("leitura", JSONObject()
                    .put("type", "string")
                    .put("description", "Uma frase curta: o que a outra pessoa quis dizer ou está sentindo."))
                .put("options", JSONObject()
                    .put("type", "array")
                    .put("description", "3 mensagens diferentes, prontas para enviar.")
                    .put("items", JSONObject().put("type", "string"))))
            .put("required", JSONArray().put("options")))
}
