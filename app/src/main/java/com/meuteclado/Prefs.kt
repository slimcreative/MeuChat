package com.meuteclado

import android.content.Context

/** Configurações guardadas no aparelho (fica na área privada do app). */
object Prefs {
    private const val FILE = "cfg"
    private const val KEY_API = "api_key"

    private fun sp(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun apiKey(c: Context): String = sp(c).getString(KEY_API, "") ?: ""

    fun saveApiKey(c: Context, key: String) {
        sp(c).edit().putString(KEY_API, key.trim()).apply()
    }
}
