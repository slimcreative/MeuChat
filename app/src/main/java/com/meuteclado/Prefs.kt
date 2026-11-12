package com.meuteclado

import android.content.Context

/** Configurações guardadas no aparelho (fica na área privada do app). */
object Prefs {
    private const val FILE = "cfg"
    private const val KEY_API = "api_key"
    private const val KEY_STYLE = "style"
    private const val KEY_MODE = "mode"
    private const val KEY_TOPIC = "topic"
    private const val KEY_TONE = "tone"
    private const val KEY_THUMB = "show_thumb"

    private fun sp(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun apiKey(c: Context): String = sp(c).getString(KEY_API, "") ?: ""

    fun saveApiKey(c: Context, key: String) {
        sp(c).edit().putString(KEY_API, key.trim()).apply()
    }

    fun mode(c: Context): String {
        val m = sp(c).getString(KEY_MODE, Prompts.DEFAULT_MODE) ?: Prompts.DEFAULT_MODE
        return if (Prompts.modes.containsKey(m)) m else Prompts.DEFAULT_MODE
    }
    fun saveMode(c: Context, v: String) = sp(c).edit().putString(KEY_MODE, v).apply()

    /** Tema e tom são lembrados por modo. */
    fun topic(c: Context): String {
        val m = mode(c)
        val keys = Prompts.mode(m).topics.keys
        val t = sp(c).getString("${KEY_TOPIC}_$m", null)
        return if (t != null && t in keys) t else keys.first()
    }
    fun saveTopic(c: Context, v: String) = sp(c).edit().putString("${KEY_TOPIC}_${mode(c)}", v).apply()

    fun tone(c: Context): String {
        val m = mode(c)
        val tones = Prompts.mode(m).tones
        val t = sp(c).getString("${KEY_TONE}_$m", null)
        return if (t != null && t in tones) t else tones.first()
    }
    fun saveTone(c: Context, v: String) = sp(c).edit().putString("${KEY_TONE}_${mode(c)}", v).apply()

    /** Instruções do usuário sobre o jeito dele de escrever. */
    fun style(c: Context): String = sp(c).getString(KEY_STYLE, "") ?: ""
    fun saveStyle(c: Context, v: String) = sp(c).edit().putString(KEY_STYLE, v.trim().take(2000)).apply()
    fun addStyle(c: Context, line: String) {
        val cur = style(c)
        saveStyle(c, if (cur.isEmpty()) line.trim() else cur + "\n" + line.trim())
    }

    fun showThumb(c: Context): Boolean = sp(c).getBoolean(KEY_THUMB, false)
    fun saveShowThumb(c: Context, v: Boolean) = sp(c).edit().putBoolean(KEY_THUMB, v).apply()
}
