package com.meuteclado

import android.content.Context

/** Configurações guardadas no aparelho (fica na área privada do app). */
object Prefs {
    private const val FILE = "cfg"
    private const val KEY_API = "api_key"
    private const val KEY_TOPIC = "topic"
    private const val KEY_TONE = "tone"
    private const val KEY_THUMB = "show_thumb"

    private fun sp(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun apiKey(c: Context): String = sp(c).getString(KEY_API, "") ?: ""

    fun saveApiKey(c: Context, key: String) {
        sp(c).edit().putString(KEY_API, key.trim()).apply()
    }

    fun topic(c: Context): String = sp(c).getString(KEY_TOPIC, "Resposta") ?: "Resposta"
    fun saveTopic(c: Context, v: String) = sp(c).edit().putString(KEY_TOPIC, v).apply()

    fun tone(c: Context): String = sp(c).getString(KEY_TONE, "leve") ?: "leve"
    fun saveTone(c: Context, v: String) = sp(c).edit().putString(KEY_TONE, v).apply()

    fun showThumb(c: Context): Boolean = sp(c).getBoolean(KEY_THUMB, false)
    fun saveShowThumb(c: Context, v: Boolean) = sp(c).edit().putBoolean(KEY_THUMB, v).apply()
}
