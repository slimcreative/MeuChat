package com.estudo.meuchat.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Catálogo de temas do app. Adicione novos aqui que eles aparecem na tela de Aparência. */
enum class ThemeId(val label: String) {
    VERDE("Verde"),
    AZUL("Azul"),
    ROXO("Roxo"),
    AMBAR("Âmbar")
}

/** Modo escuro: 0 = segue o sistema, 1 = sempre claro, 2 = sempre escuro. */
object DarkMode {
    const val SISTEMA = 0
    const val CLARO = 1
    const val ESCURO = 2
}

private fun esquemaClaro(id: ThemeId) = when (id) {
    ThemeId.VERDE -> lightColorScheme(
        primary = Color(0xFF186C4A), secondary = Color(0xFF4F6354), tertiary = Color(0xFF3B6470)
    )
    ThemeId.AZUL -> lightColorScheme(
        primary = Color(0xFF1456A0), secondary = Color(0xFF545F71), tertiary = Color(0xFF6C5676)
    )
    ThemeId.ROXO -> lightColorScheme(
        primary = Color(0xFF6A3EA1), secondary = Color(0xFF625B70), tertiary = Color(0xFF7E5260)
    )
    ThemeId.AMBAR -> lightColorScheme(
        primary = Color(0xFF8A5100), secondary = Color(0xFF725B41), tertiary = Color(0xFF53643E)
    )
}

private fun esquemaEscuro(id: ThemeId) = when (id) {
    ThemeId.VERDE -> darkColorScheme(
        primary = Color(0xFF8DD5AC), secondary = Color(0xFFB6CCB9), tertiary = Color(0xFFA3CDDA)
    )
    ThemeId.AZUL -> darkColorScheme(
        primary = Color(0xFFA4C8FF), secondary = Color(0xFFBCC7DC), tertiary = Color(0xFFD9BDE3)
    )
    ThemeId.ROXO -> darkColorScheme(
        primary = Color(0xFFD4BBFF), secondary = Color(0xFFCCC2DC), tertiary = Color(0xFFF0B7C6)
    )
    ThemeId.AMBAR -> darkColorScheme(
        primary = Color(0xFFFFB868), secondary = Color(0xFFE0C2A4), tertiary = Color(0xFFBACE98)
    )
}

@Composable
fun MeuChatTheme(
    themeId: ThemeId,
    darkMode: Int,
    dynamicColor: Boolean,
    content: @Composable () -> Unit
) {
    val escuro = when (darkMode) {
        DarkMode.CLARO -> false
        DarkMode.ESCURO -> true
        else -> isSystemInDarkTheme()
    }
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (escuro) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
        }
        escuro -> esquemaEscuro(themeId)
        else -> esquemaClaro(themeId)
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography(),
        content = content
    )
}
