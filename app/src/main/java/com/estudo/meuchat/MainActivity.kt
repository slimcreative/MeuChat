package com.estudo.meuchat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.estudo.meuchat.data.SettingsStore
import com.estudo.meuchat.ui.screens.AccountsScreen
import com.estudo.meuchat.ui.screens.AppearanceScreen
import com.estudo.meuchat.ui.screens.ChatListScreen
import com.estudo.meuchat.ui.screens.ConversationScreen
import com.estudo.meuchat.ui.screens.PrivacyScreen
import com.estudo.meuchat.ui.screens.SettingsHomeScreen
import com.estudo.meuchat.ui.theme.DarkMode
import com.estudo.meuchat.ui.theme.MeuChatTheme
import com.estudo.meuchat.ui.theme.ThemeId

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val store = SettingsStore(applicationContext)
        setContent {
            val tema by store.temaId.collectAsStateWithLifecycle(initialValue = ThemeId.VERDE)
            val modo by store.modoEscuro.collectAsStateWithLifecycle(initialValue = DarkMode.SISTEMA)
            val dinamica by store.corDinamica.collectAsStateWithLifecycle(initialValue = false)
            MeuChatTheme(themeId = tema, darkMode = modo, dynamicColor = dinamica) {
                AppRoot(store)
            }
        }
    }
}

private sealed class Aba(val rota: String, val titulo: String) {
    data object Conversas : Aba("conversas", "Conversas")
    data object Config : Aba("config", "Configurações")
}

@Composable
fun AppRoot(store: SettingsStore) {
    val nav = rememberNavController()
    val abas = listOf(Aba.Conversas, Aba.Config)
    Scaffold(
        bottomBar = {
            val entry by nav.currentBackStackEntryAsState()
            val atual = entry?.destination
            val rotaAtual = atual?.route
            if (rotaAtual == Aba.Conversas.rota || rotaAtual == Aba.Config.rota) {
                NavigationBar {
                    abas.forEach { aba ->
                        val selecionada = atual?.hierarchy?.any { it.route == aba.rota } == true
                        NavigationBarItem(
                            selected = selecionada,
                            onClick = {
                                nav.navigate(aba.rota) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = {
                                Icon(
                                    if (aba is Aba.Conversas) Icons.Filled.Chat else Icons.Filled.Settings,
                                    contentDescription = aba.titulo
                                )
                            },
                            label = { Text(aba.titulo) }
                        )
                    }
                }
            }
        }
    ) { pad ->
        NavHost(
            navController = nav,
            startDestination = Aba.Conversas.rota,
            modifier = Modifier.padding(pad)
        ) {
            composable(Aba.Conversas.rota) {
                ChatListScreen(onAbrirConversa = { id -> nav.navigate("conversa/$id") })
            }
            composable("conversa/{id}") { back ->
                val id = back.arguments?.getString("id") ?: ""
                ConversationScreen(conversaId = id, onVoltar = { nav.popBackStack() })
            }
            composable(Aba.Config.rota) {
                SettingsHomeScreen(
                    onPrivacidade = { nav.navigate("privacidade") },
                    onAparencia = { nav.navigate("aparencia") },
                    onContas = { nav.navigate("contas") }
                )
            }
            composable("privacidade") { PrivacyScreen(store = store, onVoltar = { nav.popBackStack() }) }
            composable("aparencia") { AppearanceScreen(store = store, onVoltar = { nav.popBackStack() }) }
            composable("contas") { AccountsScreen(onVoltar = { nav.popBackStack() }) }
        }
    }
}
