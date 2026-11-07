package com.estudo.meuchat.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.estudo.meuchat.data.Conta
import com.estudo.meuchat.data.Mock
import com.estudo.meuchat.data.SettingsStore
import com.estudo.meuchat.ui.theme.DarkMode
import com.estudo.meuchat.ui.theme.ThemeId
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsHomeScreen(
    onPrivacidade: () -> Unit,
    onAparencia: () -> Unit,
    onContas: () -> Unit
) {
    Scaffold(topBar = { TopAppBar(title = { Text("Configurações") }) }) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            ItemConfig(Icons.Filled.Lock, "Privacidade", "Visto por último, online, confirmações", onPrivacidade)
            ItemConfig(Icons.Filled.Palette, "Aparência", "Temas, modo escuro, cor dinâmica", onAparencia)
            ItemConfig(Icons.Filled.AccountCircle, "Contas", "Múltiplas contas no mesmo app", onContas)
        }
    }
}

@Composable
private fun ItemConfig(icone: ImageVector, titulo: String, subtitulo: String, onClick: () -> Unit) {
    ListItem(
        headlineContent = { Text(titulo) },
        supportingContent = { Text(subtitulo) },
        leadingContent = { Icon(icone, contentDescription = null) },
        modifier = Modifier.clickable(onClick = onClick)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyScreen(store: SettingsStore, onVoltar: () -> Unit) {
    val scope = rememberCoroutineScope()
    val ocultarVisto by store.ocultarVisto.collectAsStateWithLifecycle(initialValue = false)
    val ocultarOnline by store.ocultarOnline.collectAsStateWithLifecycle(initialValue = false)
    val ocultarDigitando by store.ocultarDigitando.collectAsStateWithLifecycle(initialValue = false)
    val confirmaLeitura by store.confirmaLeitura.collectAsStateWithLifecycle(initialValue = true)
    val antiRevoke by store.antiRevoke.collectAsStateWithLifecycle(initialValue = false)
    val biometrico by store.bloqueioBiometrico.collectAsStateWithLifecycle(initialValue = false)

    TelaComVoltar("Privacidade", onVoltar) {
        SwitchRow("Ocultar 'visto por último'", "Ninguém vê quando você esteve online", ocultarVisto) { v -> scope.launch { store.setOcultarVisto(v) } }
        SwitchRow("Ocultar 'online'", "Esconde seu status em tempo real", ocultarOnline) { v -> scope.launch { store.setOcultarOnline(v) } }
        SwitchRow("Ocultar 'digitando...'", "Não mostra quando você está escrevendo", ocultarDigitando) { v -> scope.launch { store.setOcultarDigitando(v) } }
        SwitchRow("Enviar confirmação de leitura", "Se desligado, você não envia o 'tique azul'", confirmaLeitura) { v -> scope.launch { store.setConfirmaLeitura(v) } }
        SwitchRow("Anti-revoke", "Mantém visíveis as mensagens que apagarem pra você", antiRevoke) { v -> scope.launch { store.setAntiRevoke(v) } }
        SwitchRow("Bloqueio biométrico", "Pede digital/rosto ao abrir o app", biometrico) { v -> scope.launch { store.setBloqueioBiometrico(v) } }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppearanceScreen(store: SettingsStore, onVoltar: () -> Unit) {
    val scope = rememberCoroutineScope()
    val tema by store.temaId.collectAsStateWithLifecycle(initialValue = ThemeId.VERDE)
    val modo by store.modoEscuro.collectAsStateWithLifecycle(initialValue = DarkMode.SISTEMA)
    val dinamica by store.corDinamica.collectAsStateWithLifecycle(initialValue = false)

    TelaComVoltar("Aparência", onVoltar) {
        Text("Tema", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(16.dp))
        ThemeId.entries.forEach { t ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(selected = tema == t, onClick = { scope.launch { store.setTema(t) } })
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = tema == t, onClick = { scope.launch { store.setTema(t) } })
                Spacer(Modifier.width(8.dp))
                Text(t.label)
            }
        }
        HorizontalDivider()
        Text("Modo escuro", style = MaterialTheme.typography.titleSmall, modifier = Modifier.padding(16.dp))
        val opcoes = listOf(
            DarkMode.SISTEMA to "Sistema",
            DarkMode.CLARO to "Claro",
            DarkMode.ESCURO to "Escuro"
        )
        SingleChoiceSegmentedButtonRow(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            opcoes.forEachIndexed { index, par ->
                SegmentedButton(
                    selected = modo == par.first,
                    onClick = { scope.launch { store.setModoEscuro(par.first) } },
                    shape = SegmentedButtonDefaults.itemShape(index = index, count = opcoes.size)
                ) { Text(par.second) }
            }
        }
        Spacer(Modifier.height(8.dp))
        SwitchRow("Cor dinâmica (Material You)", "Usa as cores do papel de parede (Android 12+)", dinamica) { v -> scope.launch { store.setCorDinamica(v) } }
    }
}

@Composable
fun AccountsScreen(onVoltar: () -> Unit) {
    val contas = remember { Mock.contas.toMutableStateList() }
    TelaComVoltar("Contas", onVoltar) {
        contas.forEach { c ->
            ListItem(
                headlineContent = { Text(c.nome) },
                supportingContent = { Text(c.numero) },
                leadingContent = { Icon(Icons.Filled.AccountCircle, contentDescription = null) },
                trailingContent = { if (c.ativa) AssistChip(onClick = {}, label = { Text("Ativa") }) }
            )
            HorizontalDivider()
        }
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = {
                contas.add(Conta((contas.size + 1).toString(), "Nova conta", "+55 11 9----", ativa = false))
            },
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Adicionar conta")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TelaComVoltar(
    titulo: String,
    onVoltar: () -> Unit,
    conteudo: @Composable ColumnScope.() -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) { conteudo() }
    }
}

@Composable
private fun SwitchRow(
    titulo: String,
    subtitulo: String,
    valor: Boolean,
    onChange: (Boolean) -> Unit
) {
    ListItem(
        headlineContent = { Text(titulo) },
        supportingContent = { Text(subtitulo) },
        trailingContent = { Switch(checked = valor, onCheckedChange = onChange) },
        modifier = Modifier.clickable { onChange(!valor) }
    )
}
