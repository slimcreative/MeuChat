package com.meuteclado

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val REQ_OVERLAY = 1
    private val REQ_PROJECTION = 2

    private val BG = 0xFF0B0C11.toInt()
    private val CARD = 0xFF171A22.toInt()
    private val FIELD = 0xFF1F232D.toInt()
    private val ACCENT = 0xFFFF8A1E.toInt()
    private val MUTED = 0xFF8D96A3.toInt()

    private lateinit var home: View
    private lateinit var settings: View
    private lateinit var status: TextView
    private lateinit var modeList: LinearLayout
    private lateinit var stopBtn: TextView
    private lateinit var permStatus: TextView
    private lateinit var keyField: EditText
    private lateinit var styleField: EditText
    private lateinit var a11yCard: View
    private lateinit var appsCard: TextView
    private var appsView: View? = null
    private var screen = 0  // 0 = principal, 1 = configurações, 2 = apps

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = BG
        window.navigationBarColor = BG
        window.decorView.setBackgroundColor(BG)

        home = buildHome()
        settings = buildSettings()
        showHome()

        if (Build.VERSION.SDK_INT >= 33) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 0)
        }
    }

    override fun onResume() {
        super.onResume()
        if (!styleField.hasFocus()) styleField.setText(Prefs.style(this))
        refresh()
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        when (screen) {
            2 -> showSettings()
            1 -> showHome()
            else -> super.onBackPressed()
        }
    }

    // ---------- peças de interface ----------

    private fun dp(n: Int): Int = (n * resources.displayMetrics.density).toInt()

    private fun round(color: Int, radiusDp: Int, stroke: Int = 0) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
        if (stroke != 0) setStroke(dp(2), stroke)
    }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false) =
        TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, Typeface.BOLD)
        }

    private fun section(text: String) = label(text, 13f, MUTED, true).apply {
        setPadding(dp(4), dp(22), 0, dp(8))
    }

    private fun pill(text: String, color: Int, onClick: () -> Unit) =
        label(text, 15f, Color.WHITE, true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(16), dp(13), dp(16), dp(13))
            background = round(color, 14)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dp(10), 0, 0) }
            setOnClickListener { onClick() }
        }

    private fun field(hintText: String) = EditText(this).apply {
        hint = hintText
        textSize = 15f
        setTextColor(Color.WHITE)
        setHintTextColor(MUTED)
        background = round(FIELD, 12)
        setPadding(dp(14), dp(12), dp(14), dp(12))
    }

    private fun fox(sizeDp: Int) = ImageView(this).apply {
        setImageResource(R.drawable.fox_bubble)
        scaleType = ImageView.ScaleType.CENTER_CROP
        background = GradientDrawable().apply {
            shape = GradientDrawable.OVAL
            setColor(0xFF1A1438.toInt())
        }
        clipToOutline = true
        layoutParams = LinearLayout.LayoutParams(dp(sizeDp), dp(sizeDp))
    }

    /** Coluna já vertical: os filhos entram ocupando a largura toda. */
    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

    private fun page(content: LinearLayout, fill: Boolean = false) = ScrollView(this).apply {
        fitsSystemWindows = true  // não entra embaixo da barra de status
        setBackgroundColor(BG)
        isVerticalScrollBarEnabled = false
        isFillViewport = fill
        content.orientation = LinearLayout.VERTICAL
        content.setPadding(dp(18), dp(18), dp(18), dp(28))
        addView(content)
    }

    // ---------- tela principal ----------

    private fun buildHome(): View {
        status = label("", 13f, MUTED)
        val titles = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), 0, 0, 0)
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            addView(label("Resposta IA", 22f, Color.WHITE, true))
            addView(status)
        }
        val gear = label("⚙", 24f, Color.WHITE).apply {
            gravity = Gravity.CENTER
            background = round(CARD, 14)
            layoutParams = LinearLayout.LayoutParams(dp(46), dp(46))
            setOnClickListener { showSettings() }
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(fox(50))
            addView(titles)
            addView(gear)
        }
        // Os cartões dividem a altura que sobra, para a tela ficar cheia
        modeList = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f)
        }
        a11yCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = round(0xFF2A1D0E.toInt(), 18, ACCENT)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dp(16), 0, 0) }
            addView(label("Ative a acessibilidade", 16f, Color.WHITE, true))
            addView(label(
                "Com ela a bolha lê a tela sem pedir compartilhamento, aparece sozinha nos apps escolhidos e envia a resposta. Toque aqui.",
                13f, 0xFFE0C9A8.toInt()
            ))
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        }
        stopBtn = pill("Desligar bolha", 0xFF3A2026.toInt()) {
            Prefs.setEnabled(this, false)
            stopService(Intent(this, CaptureService::class.java))
            status.postDelayed({ refresh() }, 300)
        }
        return page(column().apply {
            addView(header)
            addView(a11yCard)
            addView(section("TOQUE NO MODO PARA LIGAR A BOLHA"))
            addView(modeList)
            addView(stopBtn)
        }, fill = true)
    }

    private fun renderModes() {
        modeList.removeAllViews()
        val current = Prefs.mode(this)
        Prompts.modes.forEach { (name, mode) ->
            val selected = name == current
            val texts = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                addView(label(name, 20f, Color.WHITE, true))
                addView(label(mode.desc, 14f, MUTED))
                if (selected) addView(label(
                    if (CaptureService.running) "● ativo agora" else "● selecionado", 12f, ACCENT
                ).apply { setPadding(0, dp(4), 0, 0) })
            }
            modeList.addView(LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(20), dp(14), dp(16), dp(14))
                minimumHeight = dp(92)
                background = round(CARD, 20, if (selected) ACCENT else 0)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
                ).apply { setMargins(0, 0, 0, dp(10)) }
                addView(texts)
                addView(fox(60))
                setOnClickListener { openMode(name) }
            })
        }
    }

    /** Toque no modo: guarda a escolha e liga a bolha já nesse modo. */
    private fun openMode(name: String) {
        Prefs.saveMode(this, name)
        refresh()
        if (Prefs.apiKey(this).isBlank()) {
            toast("Salve a chave da API primeiro")
            showSettings()
            return
        }
        Prefs.setEnabled(this, true)
        if (CaptureService.running) {
            toast("Modo $name ativo")
            moveTaskToBack(true)
        } else {
            startFlow()
        }
    }

    // ---------- configurações ----------

    private fun buildSettings(): View {
        keyField = field("sk-ant-...").apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            setText(Prefs.apiKey(this@MainActivity))
        }
        styleField = field("Como você escreve?").apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE or
                InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            minLines = 3
            maxLines = 10
            gravity = Gravity.TOP
        }
        permStatus = label("", 14f, Color.WHITE)
        appsCard = pill("", FIELD) { showApps() }
        val back = label("←  Configurações", 20f, Color.WHITE, true).apply {
            setPadding(0, dp(6), 0, dp(6))
            setOnClickListener { showHome() }
        }
        return page(column().apply {
            addView(back)

            addView(section("MEU JEITO DE RESPONDER"))
            addView(label(
                "Ensine a IA a escrever como você. Uma instrução por linha, e pode colar mensagens suas como exemplo. " +
                    "Ex.: escrevo tudo em minúsculo, sem ponto final · uso kkkk e mano · mensagens bem curtas.",
                13f, MUTED
            ).apply { setPadding(dp(4), 0, 0, dp(10)) })
            addView(styleField)
            addView(pill("Salvar meu jeito", ACCENT) {
                Prefs.saveStyle(this@MainActivity, styleField.text.toString())
                toast("Salvo")
    
            addView(section("APPS COM A BOLHA"))
            addView(appsCard)

            addView(section("PERMISSÕES"))
            addView(permStatus)
            addView(pill("Acessibilidade (colocar no campo e enviar)", FIELD) {
                startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            })
            addView(pill("Exibir sobre outros apps", FIELD) {
                startActivity(
                    Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName"))
                )
            })

            addView(section("CHAVE DA API"))
            addView(keyField)
            addView(pill("Salvar chave", ACCENT) {
                Prefs.saveApiKey(this@MainActivity, keyField.text.toString())
                toast("Chave salva")
                refresh()
            })
        })
        })
    }

    private fun showHome() {
        screen = 0
        setContentView(home)
        home.requestApplyInsets()
        refresh()
    }

    private fun showSettings() {
        screen = 1
        setContentView(settings)
        settings.requestApplyInsets()
        refresh()
    }

    // ---------- escolha dos apps ----------

    private val favorites = listOf(
        "whatsapp", "instagram", "telegram", "messenger", "facebook.orca", "tinder", "bumble",
        "badoo", "happn", "discord", "twitter", "linkedin", "tiktok", "snapchat", "messaging", "signal"
    )

    private fun showApps() {
        screen = 2
        val v = appsView ?: buildApps().also { appsView = it }
        setContentView(v)
        v.requestApplyInsets()
    }

    private fun buildApps(): View {
        val pm = packageManager
        val launch = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val chosen = Prefs.apps(this).toMutableSet()
        val apps = pm.queryIntentActivities(launch, 0)
            .map { it.activityInfo }
            .filter { it.packageName != packageName }
            .distinctBy { it.packageName }
            .map { Triple(it.packageName, it.loadLabel(pm).toString(), it) }
            .sortedWith(
                compareByDescending<Triple<String, String, android.content.pm.ActivityInfo>> { it.first in chosen }
                    .thenByDescending { t -> favorites.any { t.first.contains(it) } }
                    .thenBy { it.second.lowercase() }
            )

        val list = column()
        list.addView(label("←  Apps com a bolha", 20f, Color.WHITE, true).apply {
            setPadding(0, dp(6), 0, dp(6))
            setOnClickListener { showSettings() }
        })
        list.addView(label(
            "Marque os apps onde a bolha deve aparecer sozinha. Sem nenhum marcado, ela aparece em qualquer app. " +
                "Precisa da acessibilidade ativa.",
            13f, MUTED
        ).apply { setPadding(dp(4), dp(6), 0, dp(14)) })

        apps.forEach { (pkg, name, info) ->
            val mark = label("", 16f, Color.WHITE, true).apply {
                gravity = Gravity.CENTER
                layoutParams = LinearLayout.LayoutParams(dp(30), dp(30))
            }
            fun paint() {
                val on = pkg in chosen
                mark.text = if (on) "✓" else ""
                mark.background = round(if (on) ACCENT else FIELD, 15)
            }
            paint()
            list.addView(LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(dp(14), dp(10), dp(14), dp(10))
                background = round(CARD, 14)
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, 0, 0, dp(8)) }
                addView(ImageView(this@MainActivity).apply {
                    runCatching { setImageDrawable(info.loadIcon(pm)) }
                    layoutParams = LinearLayout.LayoutParams(dp(36), dp(36))
                })
                addView(label(name, 16f, Color.WHITE).apply {
                    setPadding(dp(12), 0, dp(8), 0)
                    layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                })
                addView(mark)
                setOnClickListener {
                    if (pkg in chosen) chosen.remove(pkg) else chosen.add(pkg)
                    Prefs.saveApps(this@MainActivity, chosen)
                    paint()
                }
            })
        }
        return page(list)
    }

    // ---------- ligar a bolha ----------

    private fun startFlow() {
        if (!Settings.canDrawOverlays(this)) {
            startActivityForResult(
                Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")),
                REQ_OVERLAY
            )
            return
        }
        if (ReplyAccessibilityService.canCapture()) {
            // Com a acessibilidade ativa não precisa compartilhar a tela
            CaptureService.showFor(this)
            toast("Bolha ligada no modo ${Prefs.mode(this)}")
            status.postDelayed({ refresh() }, 400)
            moveTaskToBack(true)
            return
        }
        toast("Sem acessibilidade: vou pedir o compartilhamento de tela")
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
                toast("Bolha ligada no modo ${Prefs.mode(this)}")
                moveTaskToBack(true)
            }
        }
        status.postDelayed({ refresh() }, 400)
    }

    private fun refresh() {
        val running = CaptureService.running
        status.text = if (running) "Bolha ligada · modo ${Prefs.mode(this)}" else "Bolha desligada"
        stopBtn.visibility = if (running) View.VISIBLE else View.GONE
        renderModes()

        a11yCard.visibility = if (ReplyAccessibilityService.instance != null) View.GONE else View.VISIBLE
        val n = Prefs.apps(this).size
        appsCard.text = if (n == 0) "Escolher apps (agora: todos)" else "Escolher apps ($n marcados)"

        val key = if (Prefs.apiKey(this).isNotBlank()) "salva ✓" else "não definida"
        val ov = if (Settings.canDrawOverlays(this)) "ok ✓" else "pendente"
        val ac = if (ReplyAccessibilityService.instance != null) "ativa ✓" else "inativa"
        permStatus.text = "Chave da API: $key\nSobreposição: $ov\nAcessibilidade: $ac"
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
