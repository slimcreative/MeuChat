package com.meuteclado

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Typeface
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val REQ_OVERLAY = 1
    private val REQ_PROJECTION = 2
    private lateinit var status: TextView
    private lateinit var styleField: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (16 * resources.displayMetrics.density).toInt()

        status = TextView(this).apply { textSize = 15f }
        val keyField = EditText(this).apply {
            hint = "Chave da API (sk-ant-...)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setText(Prefs.apiKey(this@MainActivity))
        }

        styleField = EditText(this).apply {
            hint = "Como você escreve?"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            minLines = 3
            maxLines = 10
            gravity = Gravity.TOP
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(pad, pad, pad, pad)
            addView(status)
            addView(keyField)
            addView(button("Salvar chave") {
                Prefs.saveApiKey(this@MainActivity, keyField.text.toString())
                refresh()
            })
            addView(button("Ativar bolha") { startFlow() })
            addView(button("Desativar") {
                stopService(Intent(this@MainActivity, CaptureService::class.java))
                refresh()
            })
            addView(button("Abrir acessibilidade (inserir e enviar)") {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            })
            addView(TextView(this@MainActivity).apply {
                text = "Modo de conversa"
                textSize = 18f
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, pad, 0, pad / 2)
            })
            addView(modePicker())
            addView(TextView(this@MainActivity).apply {
                text = "Meu jeito de responder"
                textSize = 18f
                setTypeface(typeface, Typeface.BOLD)
                setPadding(0, pad, 0, pad / 2)
            })
            addView(TextView(this@MainActivity).apply {
                text = "Ensine a IA a escrever como você. Uma instrução por linha, e pode colar mensagens suas como exemplo. " +
                    "Ex.: escrevo tudo em minúsculo, sem ponto final · uso kkkk e mano · mensagens bem curtas · nunca uso emoji."
                textSize = 13f
            })
            addView(styleField)
            addView(button("Salvar meu jeito") {
                Prefs.saveStyle(this@MainActivity, styleField.text.toString())
                Toast.makeText(this@MainActivity, "Salvo", Toast.LENGTH_SHORT).show()
            })
        }
        setContentView(ScrollView(this).apply { addView(root) })

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!styleField.hasFocus()) styleField.setText(Prefs.style(this))
        refresh()
    }

    /** Lista de modos: a escolha vale na próxima vez que a telinha abrir. */
    private fun modePicker(): RadioGroup {
        val group = RadioGroup(this)
        val current = Prefs.mode(this)
        Prompts.modes.entries.forEachIndexed { i, (name, mode) ->
            group.addView(RadioButton(this).apply {
                id = 100 + i
                text = "$name\n${mode.desc}"
                textSize = 15f
                setPadding(0, 12, 0, 12)
                isChecked = name == current
                tag = name
            })
        }
        group.setOnCheckedChangeListener { g, checkedId ->
            val name = g.findViewById<RadioButton>(checkedId)?.tag as? String
            if (name != null) Prefs.saveMode(this, name)
        }
        return group
    }

    private fun button(label: String, onClick: () -> Unit): Button =
        Button(this).apply {
            text = label
            setOnClickListener { onClick() }
        }

    private fun startFlow() {
        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
                REQ_OVERLAY
            )
            return
        }
        val mpm = getSystemService(MediaProjectionManager::class.java)
        startActivityForResult(mpm.createScreenCaptureIntent(), REQ_PROJECTION)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQ_OVERLAY -> if (Settings.canDrawOverlays(this)) startFlow()
            REQ_PROJECTION -> if (resultCode == RESULT_OK && data != null) {
                val i = Intent(this, CaptureService::class.java)
                    .putExtra("code", resultCode)
                    .putExtra("data", data)
                startForegroundService(i)
            }
        }
        refresh()
    }

    private fun refresh() {
        val key = if (Prefs.apiKey(this).isNotBlank()) "salva" else "não definida"
        val ov = if (Settings.canDrawOverlays(this)) "ok" else "pendente"
        val sv = if (CaptureService.running) "ativa" else "inativa"
        val ac = if (ReplyAccessibilityService.instance != null) "ativa" else "inativa"
        status.text = "Chave da API: $key\nSobreposição: $ov\nBolha: $sv\nInserção automática: $ac"
    }
}
