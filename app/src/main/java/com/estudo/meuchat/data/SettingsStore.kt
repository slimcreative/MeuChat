package com.estudo.meuchat.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.estudo.meuchat.ui.theme.DarkMode
import com.estudo.meuchat.ui.theme.ThemeId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "config")

/**
 * Guarda as preferências do usuário em disco (DataStore) e as expõe como Flow,
 * pra UI reagir na hora que muda. É aqui que vivem as opções estilo "mod".
 */
class SettingsStore(private val context: Context) {

    private object Keys {
        val TEMA = stringPreferencesKey("tema")
        val MODO_ESCURO = intPreferencesKey("modo_escuro")
        val COR_DINAMICA = booleanPreferencesKey("cor_dinamica")
        val OCULTAR_VISTO = booleanPreferencesKey("ocultar_visto")
        val OCULTAR_ONLINE = booleanPreferencesKey("ocultar_online")
        val OCULTAR_DIGITANDO = booleanPreferencesKey("ocultar_digitando")
        val CONFIRMA_LEITURA = booleanPreferencesKey("confirma_leitura")
        val ANTI_REVOKE = booleanPreferencesKey("anti_revoke")
        val BLOQUEIO_BIOMETRICO = booleanPreferencesKey("bloqueio_biometrico")
    }

    // ----- Aparência -----
    val temaId: Flow<ThemeId> = context.dataStore.data.map { prefs ->
        runCatching { ThemeId.valueOf(prefs[Keys.TEMA] ?: ThemeId.VERDE.name) }
            .getOrDefault(ThemeId.VERDE)
    }
    val modoEscuro: Flow<Int> = context.dataStore.data.map { it[Keys.MODO_ESCURO] ?: DarkMode.SISTEMA }
    val corDinamica: Flow<Boolean> = context.dataStore.data.map { it[Keys.COR_DINAMICA] ?: false }

    // ----- Privacidade -----
    val ocultarVisto: Flow<Boolean> = context.dataStore.data.map { it[Keys.OCULTAR_VISTO] ?: false }
    val ocultarOnline: Flow<Boolean> = context.dataStore.data.map { it[Keys.OCULTAR_ONLINE] ?: false }
    val ocultarDigitando: Flow<Boolean> = context.dataStore.data.map { it[Keys.OCULTAR_DIGITANDO] ?: false }
    val confirmaLeitura: Flow<Boolean> = context.dataStore.data.map { it[Keys.CONFIRMA_LEITURA] ?: true }
    val antiRevoke: Flow<Boolean> = context.dataStore.data.map { it[Keys.ANTI_REVOKE] ?: false }
    val bloqueioBiometrico: Flow<Boolean> = context.dataStore.data.map { it[Keys.BLOQUEIO_BIOMETRICO] ?: false }

    // ----- Escritas -----
    suspend fun setTema(v: ThemeId) = context.dataStore.edit { it[Keys.TEMA] = v.name }
    suspend fun setModoEscuro(v: Int) = context.dataStore.edit { it[Keys.MODO_ESCURO] = v }.let {}
    suspend fun setCorDinamica(v: Boolean) = context.dataStore.edit { it[Keys.COR_DINAMICA] = v }.let {}
    suspend fun setOcultarVisto(v: Boolean) = context.dataStore.edit { it[Keys.OCULTAR_VISTO] = v }.let {}
    suspend fun setOcultarOnline(v: Boolean) = context.dataStore.edit { it[Keys.OCULTAR_ONLINE] = v }.let {}
    suspend fun setOcultarDigitando(v: Boolean) = context.dataStore.edit { it[Keys.OCULTAR_DIGITANDO] = v }.let {}
    suspend fun setConfirmaLeitura(v: Boolean) = context.dataStore.edit { it[Keys.CONFIRMA_LEITURA] = v }.let {}
    suspend fun setAntiRevoke(v: Boolean) = context.dataStore.edit { it[Keys.ANTI_REVOKE] = v }.let {}
    suspend fun setBloqueioBiometrico(v: Boolean) = context.dataStore.edit { it[Keys.BLOQUEIO_BIOMETRICO] = v }.let {}
}
