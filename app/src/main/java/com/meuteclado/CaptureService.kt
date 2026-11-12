package com.meuteclado

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.Icon
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.Image
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Base64
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.content.Context
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import kotlin.math.abs

class CaptureService : Service() {

    companion object {
        const val ACTION_STOP = "com.meuteclado.STOP"
        const val CHANNEL = "resposta_ia"
        @Volatile var running = false
        private const val BUBBLE_DP = 48
        private const val ACCENT = 0xFF1E88E5.toInt()
        private const val CHIP = 0xFF2A303C.toInt()
    }

    private val main = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var wm: WindowManager
    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null
    private var vDisplay: VirtualDisplay? = null
    private var bubble: View? = null
    private var panel: View? = null
    private var dismiss: View? = null
    private var bubbleLp: WindowManager.LayoutParams? = null
    private var panelLp: WindowManager.LayoutParams? = null

    private var topicRow: LinearLayout? = null
    private var toneRow: LinearLayout? = null
    private var thumbView: ImageView? = null
    private var noteView: TextView? = null
    private var statusView: TextView? = null
    private var resultsView: LinearLayout? = null

    private var latest: Image? = null
    private var capturing = false
    private var lastShot: String? = null
    private var lastThumb: Bitmap? = null
    private var shotBlack = false
    private var job: Job? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            shutdown()
            return START_NOT_STICKY
        }
        createChannel()
        startForeground(1, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)

        if (projection == null) {
            val code = intent?.getIntExtra("code", 0) ?: 0
            val data = dataExtra(intent)
            if (code == 0 || data == null) {
                shutdown()
                return START_NOT_STICKY
            }
            val mp = getSystemService(MediaProjectionManager::class.java)
                .getMediaProjection(code, data)
            mp.registerCallback(object : MediaProjection.Callback() {
                override fun onStop() {
                    main.post { shutdown() }
                }
            }, main)
            projection = mp
            setupDisplay(mp)
            showBubble()
            running = true
        }
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        scope.cancel()
        shutdown()
        super.onDestroy()
    }

    // ---------- captura ----------

    private fun setupDisplay(mp: MediaProjection) {
        val m = resources.displayMetrics
        val w = m.widthPixels
        val h = m.heightPixels
        val r = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 4)
        // Guarda sempre o quadro mais recente; a captura usa esse quadro.
        r.setOnImageAvailableListener({ ir ->
            val img = try { ir.acquireLatestImage() } catch (e: Exception) { null }
            if (img != null) {
                latest?.close()
                latest = img
            }
        }, main)
        reader = r
        vDisplay = mp.createVirtualDisplay(
            "meuteclado", w, h, m.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            r.surface, null, null
        )
    }

    private fun toBitmap(img: Image): Bitmap {
        val w = img.width
        val h = img.height
        val plane = img.planes[0]
        val padded = Bitmap.createBitmap(
            w + (plane.rowStride - plane.pixelStride * w) / plane.pixelStride,
            h, Bitmap.Config.ARGB_8888
        )
        plane.buffer.rewind()
        padded.copyPixelsFromBuffer(plane.buffer)
        return Bitmap.createBitmap(padded, 0, 0, w, h)
    }

    private fun toJpegB64(bmp: Bitmap): String {
        val long = maxOf(bmp.width, bmp.height)
        val k = if (long > 1568) 1568f / long else 1f
        val scaled = Bitmap.createScaledBitmap(
            bmp, (bmp.width * k).toInt(), (bmp.height * k).toInt(), true
        )
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 82, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    /** Esconde bolha e painel, pega o quadro seguinte da tela e mostra tudo de volta. */
    private fun capture(onShot: (Bitmap) -> Unit) {
        if (capturing) return
        capturing = true
        bubble?.visibility = View.INVISIBLE
        panel?.visibility = View.INVISIBLE
        var tries = 0
        lateinit var attempt: Runnable
        attempt = Runnable {
            val bmp = latest?.let { img -> runCatching { toBitmap(img) }.getOrNull() }
            if (bmp == null && tries++ < 6) {
                main.postDelayed(attempt, 150)
                return@Runnable
            }
            capturing = false
            bubble?.visibility = View.VISIBLE
            panel?.visibility = View.VISIBLE
            if (bmp == null) toast("Não consegui capturar a tela") else onShot(bmp)
        }
        main.postDelayed(attempt, 220)
    }

    // ---------- bolha flutuante ----------

    private fun dp(n: Int): Int = (n * resources.displayMetrics.density).toInt()

    private fun overlayParams(w: Int, h: Int, extraFlags: Int = 0) =
        WindowManager.LayoutParams(
            w, h,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or extraFlags,
            PixelFormat.TRANSLUCENT
        )

    private fun round(color: Int, radiusDp: Int, strokeColor: Int = 0) = GradientDrawable().apply {
        setColor(color)
        cornerRadius = dp(radiusDp).toFloat()
        if (strokeColor != 0) setStroke(dp(1), strokeColor)
    }

    private fun showBubble() {
        val size = dp(BUBBLE_DP)
        val tv = TextView(this).apply {
            text = "IA"
            textSize = 14f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ACCENT)
                setStroke(dp(2), 0x55FFFFFF)
            }
        }
        val lp = overlayParams(size, size).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = dp(300)
        }

        val slop = dp(8)
        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        var dragging = false
        tv.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = lp.x; startY = lp.y
                    touchX = e.rawX; touchY = e.rawY
                    dragging = false
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!dragging && (abs(e.rawX - touchX) > slop || abs(e.rawY - touchY) > slop)) {
                        dragging = true
                        showDismissTarget()
                    }
                    if (dragging) {
                        lp.x = startX + (e.rawX - touchX).toInt()
                        lp.y = startY + (e.rawY - touchY).toInt()
                        wm.updateViewLayout(tv, lp)
                        positionPanel()
                        highlightDismiss(overDismiss(tv))
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (!dragging) {
                        onBubbleTap()
                    } else {
                        val close = overDismiss(tv)
                        hideDismissTarget()
                        if (close) {
                            main.post { shutdown() }
                        } else {
                            // encosta na borda mais próxima
                            val sw = resources.displayMetrics.widthPixels
                            lp.x = if (lp.x + size / 2 < sw / 2) 0 else sw - size
                            wm.updateViewLayout(tv, lp)
                            positionPanel()
                        }
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    hideDismissTarget()
                    true
                }
                else -> false
            }
        }
        wm.addView(tv, lp)
        bubble = tv
        bubbleLp = lp
    }

    /** Toque na bolha: abre a telinha (lê a tela e já gera) ou fecha se estiver aberta. */
    private fun onBubbleTap() {
        if (capturing) return
        if (panel != null) {
            removePanel()
            return
        }
        capture { bmp ->
            setShot(bmp)
            showPanel()
            generate()
        }
    }

    private fun setShot(bmp: Bitmap) {
        shotBlack = isMostlyBlack(bmp)
        lastShot = toJpegB64(bmp)
        val tw = dp(92)
        lastThumb = Bitmap.createScaledBitmap(bmp, tw, bmp.height * tw / bmp.width, true)
        thumbView?.setImageBitmap(lastThumb)
    }

    // ---------- alvo "solte aqui para fechar" ----------

    private fun showDismissTarget() {
        if (dismiss != null) return
        val v = TextView(this).apply {
            text = "✕"
            textSize = 22f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xCC000000.toInt())
                setStroke(dp(2), 0x88FFFFFF.toInt())
            }
        }
        val lp = overlayParams(dp(64), dp(64), WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE).apply {
            gravity = Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL
            y = dp(56)
        }
        wm.addView(v, lp)
        dismiss = v
    }

    private fun hideDismissTarget() {
        dismiss?.let { runCatching { wm.removeView(it) } }
        dismiss = null
    }

    private fun overDismiss(bub: View): Boolean {
        val d = dismiss ?: return false
        if (d.width == 0) return false
        val a = IntArray(2)
        val b = IntArray(2)
        bub.getLocationOnScreen(a)
        d.getLocationOnScreen(b)
        val dx = (a[0] + bub.width / 2) - (b[0] + d.width / 2)
        val dy = (a[1] + bub.height / 2) - (b[1] + d.height / 2)
        return abs(dx) < dp(70) && abs(dy) < dp(70)
    }

    private fun highlightDismiss(on: Boolean) {
        val d = dismiss ?: return
        (d.background as? GradientDrawable)?.setColor(if (on) 0xFFE53935.toInt() else 0xCC000000.toInt())
    }

    // ---------- telinha ao lado da bolha ----------

    private fun isMostlyBlack(bmp: Bitmap): Boolean {
        var total = 0L
        var n = 0
        for (i in 0 until 10) for (j in 0 until 10) {
            val px = bmp.getPixel(bmp.width * i / 10, bmp.height * j / 10)
            total += ((px shr 16) and 0xFF) + ((px shr 8) and 0xFF) + (px and 0xFF)
            n++
        }
        return total / (n * 3) < 8
    }

    private fun chip(label: String, selected: Boolean, onClick: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            textSize = 12f
            setTextColor(if (selected) Color.WHITE else 0xFFB8C0CC.toInt())
            setPadding(dp(10), dp(5), dp(10), dp(5))
            background = round(if (selected) ACCENT else CHIP, 14)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 0, dp(5), 0) }
            setOnClickListener { onClick() }
        }

    private fun iconButton(label: String, onClick: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = round(CHIP, 14)
            layoutParams = LinearLayout.LayoutParams(dp(30), dp(28)).apply { setMargins(dp(5), 0, 0, 0) }
            setOnClickListener { onClick() }
        }

    private fun hScroll(child: View) = HorizontalScrollView(this).apply {
        isHorizontalScrollBarEnabled = false
        addView(child)
    }

    private fun renderChips() {
        val topics = topicRow ?: return
        val tones = toneRow ?: return
        val curTopic = Prefs.topic(this)
        val curTone = Prefs.tone(this)
        topics.removeAllViews()
        val mode = Prompts.mode(Prefs.mode(this))
        mode.topics.keys.forEach { t ->
            topics.addView(chip(t, t == curTopic) {
                Prefs.saveTopic(this, t)
                renderChips()
                generate()
            })
        }
        tones.removeAllViews()
        mode.tones.forEach { t ->
            tones.addView(chip(t, t == curTone) {
                Prefs.saveTone(this, t)
                renderChips()
                generate()
            })
        }
    }

    private fun showPanel() {
        removePanel()
        val sw = resources.displayMetrics.widthPixels
        val pw = minOf(dp(264), sw - dp(BUBBLE_DP) - dp(14))

        val topics = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        val tones = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        topicRow = topics
        toneRow = tones

        val thumb = ImageView(this).apply {
            adjustViewBounds = true
            setImageBitmap(lastThumb)
            visibility = if (Prefs.showThumb(this@CaptureService)) View.VISIBLE else View.GONE
            layoutParams = LinearLayout.LayoutParams(dp(92), LinearLayout.LayoutParams.WRAP_CONTENT)
                .apply { setMargins(0, dp(8), 0, 0) }
        }
        thumbView = thumb

        lateinit var eye: TextView
        eye = iconButton("👁") {
            val show = thumb.visibility != View.VISIBLE
            thumb.visibility = if (show) View.VISIBLE else View.GONE
            eye.alpha = if (show) 1f else 0.45f
            Prefs.saveShowThumb(this, show)
        }
        eye.alpha = if (thumb.visibility == View.VISIBLE) 1f else 0.45f

        val refresh = iconButton("↻") {
            capture { bmp ->
                setShot(bmp)
                generate()
            }
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            addView(hScroll(topics).apply {
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
            })
            addView(eye)
            addView(refresh)
        }

        val note = TextView(this).apply {
            textSize = 12f
            setTextColor(0xFFFFD27A.toInt())
            setPadding(0, dp(8), 0, 0)
            visibility = View.GONE
        }
        noteView = note

        val status = TextView(this).apply {
            textSize = 12f
            setTextColor(0xFF8D96A3.toInt())
            setPadding(0, dp(8), 0, 0)
        }
        statusView = status

        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        resultsView = results
        val scroll = MaxHeightScrollView(this, dp(250)).apply {
            isVerticalScrollBarEnabled = false
            addView(results)
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(10), dp(10), dp(10))
            background = round(0xF2171B22.toInt(), 18, 0x33FFFFFF)
            addView(top)
            addView(hScroll(tones).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply { setMargins(0, dp(6), 0, 0) }
            })
            addView(thumb)
            addView(note)
            addView(status)
            addView(scroll)
        }

        val lp = overlayParams(pw, WindowManager.LayoutParams.WRAP_CONTENT).apply {
            gravity = Gravity.TOP or Gravity.START
        }
        panel = root
        panelLp = lp
        renderChips()
        placePanel(lp, 0)
        wm.addView(root, lp)
        root.addOnLayoutChangeListener { _, _, t, _, b, _, ot, _, ob ->
            if (b - t != ob - ot) main.post { positionPanel() }
        }

        // Mantém a bolha por cima para continuar tocável e arrastável
        val bub = bubble
        val blp = bubbleLp
        if (bub != null && blp != null) {
            runCatching { wm.removeView(bub) }
            wm.addView(bub, blp)
        }
    }

    /** Calcula a posição da telinha: ao lado da bolha, sem sair da tela. */
    private fun placePanel(lp: WindowManager.LayoutParams, panelHeight: Int): Boolean {
        val bl = bubbleLp ?: return false
        val m = resources.displayMetrics
        val size = dp(BUBBLE_DP)
        val gap = dp(6)
        val leftSide = bl.x + size / 2 < m.widthPixels / 2
        val x = (if (leftSide) bl.x + size + gap else bl.x - lp.width - gap)
            .coerceIn(dp(4), maxOf(dp(4), m.widthPixels - lp.width - dp(4)))
        val y = bl.y.coerceIn(0, maxOf(0, m.heightPixels - panelHeight - dp(72)))
        if (x == lp.x && y == lp.y) return false
        lp.x = x
        lp.y = y
        return true
    }

    private fun positionPanel() {
        val p = panel ?: return
        val lp = panelLp ?: return
        if (placePanel(lp, p.height)) runCatching { wm.updateViewLayout(p, lp) }
    }

    private fun setStatus(text: String) {
        val s = statusView ?: return
        s.text = text
        s.visibility = if (text.isEmpty()) View.GONE else View.VISIBLE
    }

    private fun actionPill(label: String, primary: Boolean, onClick: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            textSize = 12f
            setTextColor(Color.WHITE)
            setPadding(dp(12), dp(5), dp(12), dp(5))
            background = round(if (primary) ACCENT else 0xFF3A4250.toInt(), 13)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(dp(6), 0, 0, 0) }
            setOnClickListener { onClick() }
        }

    /** Cartão de uma sugestão: toque no texto coloca no campo; botões Copiar e Enviar. */
    private fun replyCard(text: String): View {
        val body = TextView(this).apply {
            this.text = text
            textSize = 14f
            setTextColor(Color.WHITE)
            setOnClickListener {
                if (ReplyAccessibilityService.insert(text)) {
                    toast("Coloquei no campo")
                    removePanel()
                } else {
                    copy(text, "Copiado. Cole no campo.")
                    removePanel()
                }
            }
        }
        val actions = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.END
            setPadding(0, dp(8), 0, 0)
            addView(actionPill("Copiar", false) {
                copy(text, "Copiado")
                removePanel()
            })
            addView(actionPill("Enviar", true) {
                send(text)
                removePanel()
            })
        }
        return LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(10), dp(10), dp(10))
            background = round(0xFF242A35.toInt(), 14)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, dp(8), 0, 0) }
            addView(body)
            addView(actions)
        }
    }

    private fun generate() {
        val results = resultsView ?: return
        val note = noteView ?: return
        job?.cancel()
        results.removeAllViews()
        note.visibility = View.GONE

        val shot = lastShot
        if (shot == null) {
            setStatus("Sem captura. Toque em ↻.")
            return
        }
        if (shotBlack) {
            setStatus("A captura veio preta: este app bloqueia captura de tela.")
            return
        }
        val key = Prefs.apiKey(this)
        if (key.isBlank()) {
            setStatus("Abra o app Resposta IA e salve a chave da API.")
            return
        }
        val mode = Prefs.mode(this)
        val topic = Prefs.topic(this)
        val tone = Prefs.tone(this)
        setStatus("Lendo a conversa… · modo $mode")
        job = scope.launch {
            try {
                val r = withContext(Dispatchers.IO) { AiClient.replies(key, shot, mode, topic, tone) }
                if (panel == null || resultsView !== results) return@launch
                setStatus("")
                if (r.reading.isNotEmpty()) {
                    note.text = r.reading
                    note.visibility = View.VISIBLE
                }
                r.options.forEach { results.addView(replyCard(it)) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                if (panel == null || resultsView !== results) return@launch
                setStatus(e.message ?: "Erro ao gerar a resposta. Toque em ↻.")
            }
        }
    }

    private fun copy(text: String, msg: String) {
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("resposta", text))
        toast(msg)
    }

    /** Coloca no campo e toca em enviar; sem acessibilidade ativa, copia. */
    private fun send(text: String) {
        val started = ReplyAccessibilityService.send(text) { sent ->
            if (!sent) toast("Coloquei no campo. Toque em enviar.")
        }
        if (!started) {
            copy(
                text,
                if (ReplyAccessibilityService.instance == null)
                    "Copiado. Ative a acessibilidade no app para enviar direto."
                else "Copiado. Toque no campo de mensagem e cole."
            )
        }
    }

    private fun removePanel() {
        job?.cancel()
        job = null
        panel?.let { runCatching { wm.removeView(it) } }
        panel = null
        panelLp = null
        topicRow = null
        toneRow = null
        thumbView = null
        noteView = null
        statusView = null
        resultsView = null
    }

    // ---------- ciclo de vida ----------

    private fun shutdown() {
        removePanel()
        hideDismissTarget()
        bubble?.let { runCatching { wm.removeView(it) } }
        bubble = null
        vDisplay?.release(); vDisplay = null
        runCatching { latest?.close() }; latest = null
        reader?.close(); reader = null
        projection?.stop(); projection = null
        running = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun createChannel() {
        getSystemService(NotificationManager::class.java).createNotificationChannel(
            NotificationChannel(CHANNEL, "Resposta IA", NotificationManager.IMPORTANCE_LOW)
        )
    }

    private fun buildNotification(): Notification {
        val stop = PendingIntent.getService(
            this, 0,
            Intent(this, CaptureService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL)
            .setContentTitle("Resposta IA ativa")
            .setContentText("Toque em Desativar para parar")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, android.R.drawable.ic_menu_close_clear_cancel),
                    "Desativar", stop
                ).build()
            )
            .build()
    }

    @Suppress("DEPRECATION")
    private fun dataExtra(i: Intent?): Intent? =
        if (Build.VERSION.SDK_INT >= 33) i?.getParcelableExtra("data", Intent::class.java)
        else i?.getParcelableExtra("data")

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}

/** ScrollView que cresce com o conteúdo até um limite. */
class MaxHeightScrollView(context: Context, private val maxHeight: Int) : ScrollView(context) {
    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(
            widthMeasureSpec,
            MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST)
        )
    }
}
