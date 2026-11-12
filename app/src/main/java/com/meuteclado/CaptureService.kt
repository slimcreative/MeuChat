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
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
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
    }

    private val main = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private lateinit var wm: WindowManager
    private var projection: MediaProjection? = null
    private var reader: ImageReader? = null
    private var vDisplay: VirtualDisplay? = null
    private var bubble: View? = null
    private var panel: View? = null

    @Volatile private var wantShot: ((Bitmap) -> Unit)? = null
    private var lastShot: String? = null
    private var selectedTopic = "Resposta"

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
        val r = ImageReader.newInstance(w, h, PixelFormat.RGBA_8888, 2)
        r.setOnImageAvailableListener({ ir ->
            val img = ir.acquireLatestImage() ?: return@setOnImageAvailableListener
            val cb = wantShot
            if (cb == null) {
                img.close()
                return@setOnImageAvailableListener
            }
            wantShot = null
            val bmp = toBitmap(img, w, h)
            img.close()
            cb(bmp)
        }, main)
        reader = r
        vDisplay = mp.createVirtualDisplay(
            "meuteclado", w, h, m.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
            r.surface, null, null
        )
    }

    private fun toBitmap(img: Image, w: Int, h: Int): Bitmap {
        val plane = img.planes[0]
        val padded = Bitmap.createBitmap(
            w + (plane.rowStride - plane.pixelStride * w) / plane.pixelStride,
            h, Bitmap.Config.ARGB_8888
        )
        padded.copyPixelsFromBuffer(plane.buffer)
        return Bitmap.createBitmap(padded, 0, 0, w, h)
    }

    private fun toJpegB64(bmp: Bitmap): String {
        val w = 1080
        val scaled = Bitmap.createScaledBitmap(bmp, w, bmp.height * w / bmp.width, true)
        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 80, out)
        return Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
    }

    // ---------- bolha flutuante ----------

    private fun dp(n: Int): Int = (n * resources.displayMetrics.density).toInt()

    private fun overlayParams(w: Int, h: Int) =
        WindowManager.LayoutParams(
            w, h,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )

    private fun showBubble() {
        val size = dp(56)
        val tv = TextView(this).apply {
            text = "IA"
            textSize = 15f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(0xFF1E88E5.toInt())
            }
        }
        val lp = overlayParams(size, size).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 0
            y = dp(300)
        }

        var startX = 0
        var startY = 0
        var touchX = 0f
        var touchY = 0f
        tv.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = lp.x; startY = lp.y
                    touchX = e.rawX; touchY = e.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    lp.x = startX + (e.rawX - touchX).toInt()
                    lp.y = startY + (e.rawY - touchY).toInt()
                    wm.updateViewLayout(tv, lp)
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if (abs(e.rawX - touchX) < 12 && abs(e.rawY - touchY) < 12) onBubbleTap()
                    true
                }
                else -> false
            }
        }
        wm.addView(tv, lp)
        bubble = tv
    }

    private fun onBubbleTap() {
        val bub = bubble ?: return
        removePanel()
        bub.visibility = View.INVISIBLE  // some da captura para não aparecer no print
        wantShot = { bmp ->
            bub.visibility = View.VISIBLE
            lastShot = toJpegB64(bmp)
            showPanel()
        }
        main.postDelayed({
            if (wantShot != null) {
                wantShot = null
                bub.visibility = View.VISIBLE
                toast("Não consegui capturar a tela")
            }
        }, 2500)
    }

    // ---------- painel: temas + tons ----------

    private fun showPanel() {
        removePanel()
        val status = TextView(this).apply {
            text = "Tema: $selectedTopic · escolha o tom"
            textSize = 13f
            setTextColor(Color.DKGRAY)
        }
        val results = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        val topicRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        Prompts.topics.keys.forEach { topic ->
            topicRow.addView(Button(this).apply {
                text = topic
                isAllCaps = false
                setOnClickListener {
                    selectedTopic = topic
                    status.text = "Tema: $topic · escolha o tom"
                }
            })
        }

        val toneRow = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        Prompts.tones.forEach { tone ->
            toneRow.addView(Button(this).apply {
                text = tone
                isAllCaps = false
                setOnClickListener { generate(tone, status, results) }
            })
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(12), dp(12), dp(12))
            background = GradientDrawable().apply {
                setColor(Color.WHITE)
                cornerRadius = dp(16).toFloat()
            }
            addView(HorizontalScrollView(this@CaptureService).apply { addView(topicRow) })
            addView(HorizontalScrollView(this@CaptureService).apply { addView(toneRow) })
            addView(status)
            addView(results)
            addView(Button(this@CaptureService).apply {
                text = "Fechar"
                setOnClickListener { removePanel() }
            })
        }

        val lp = overlayParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        ).apply { gravity = Gravity.BOTTOM }

        wm.addView(root, lp)
        panel = root
    }

    private fun generate(tone: String, status: TextView, results: LinearLayout) {
        val shot = lastShot ?: return
        val key = Prefs.apiKey(this)
        if (key.isBlank()) {
            status.text = "Defina a chave da API no app principal"
            return
        }
        val topic = selectedTopic
        status.text = "Gerando ($topic · $tone)..."
        results.removeAllViews()
        scope.launch {
            try {
                val options = withContext(Dispatchers.IO) {
                    AiClient.replies(key, shot, topic, tone)
                }
                status.text = "Toque numa opção para inserir"
                options.forEach { opt ->
                    results.addView(Button(this@CaptureService).apply {
                        text = opt
                        isAllCaps = false
                        setOnClickListener {
                            deliver(opt)
                            removePanel()
                        }
                    })
                }
            } catch (e: Exception) {
                status.text = "Erro: ${e.message}"
            }
        }
    }

    private fun deliver(text: String) {
        if (ReplyAccessibilityService.insert(text)) return
        val cm = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("resposta", text))
        toast("Copiado. Cole no campo.")
    }

    private fun removePanel() {
        panel?.let { runCatching { wm.removeView(it) } }
        panel = null
    }

    // ---------- ciclo de vida ----------

    private fun shutdown() {
        removePanel()
        bubble?.let { runCatching { wm.removeView(it) } }
        bubble = null
        vDisplay?.release(); vDisplay = null
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
