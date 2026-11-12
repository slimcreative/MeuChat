package com.meuteclado

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class MainActivity : Activity() {

    private val REQ_OVERLAY = 1
    private val REQ_PROJECTION = 2
    private lateinit var status: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val pad = (16 * resources.displayMetrics.density).toInt()

        status = TextView(this).apply { textSize = 15f }
        val keyField = EditText(this).apply {
            hint = "Chave da API (sk-ant-...)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setText(Prefs.apiKey(this@MainActivity))
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
            addView(button("Abrir acessibilidade (inserir texto)") {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            })
        }
        setContentView(root)

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
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
