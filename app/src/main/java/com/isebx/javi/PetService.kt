package com.isebx.javi

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.graphics.PixelFormat
import android.graphics.Point
import android.graphics.drawable.Icon
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewConfiguration
import android.view.WindowInsets
import android.view.WindowManager
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/** Mantiene a Javi caminando sobre la pantalla, por encima de las demás apps. */
class PetService : Service() {

    companion object {
        @Volatile
        var running = false

        const val ACTION_FEED = "com.isebx.javi.FEED"
        const val ACTION_PET = "com.isebx.javi.PET"
        const val ACTION_PLAY = "com.isebx.javi.PLAY"
        const val ACTION_STOP = "com.isebx.javi.STOP"
        private const val CHANNEL = "javi"
    }

    private lateinit var wm: WindowManager
    private var view: PetView? = null
    private var lp: WindowManager.LayoutParams? = null
    private val javi = Javi()
    private var unit = 1f
    private var screenW = 0
    private var floorY = 0
    private var lastNs = 0L
    private var active = true
    private var nightCheck = 0f

    private val frame = object : Choreographer.FrameCallback {
        override fun doFrame(ns: Long) {
            if (!active || view == null) return
            val dt = if (lastNs == 0L) 0.016f else min(0.05f, (ns - lastNs) / 1e9f)
            lastNs = ns
            nightCheck -= dt
            if (nightCheck <= 0f) {
                nightCheck = 60f
                val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                javi.night = h >= 23 || h < 7
            }
            javi.update(dt)
            place()
            view?.invalidate()
            if (javi.calm) {
                Choreographer.getInstance().postFrameCallbackDelayed(this, 60)
            } else {
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> active = false
                Intent.ACTION_SCREEN_ON -> resumeLoop()
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        wm = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        goForeground()
        if (!Settings.canDrawOverlays(this)) {
            stopSelf()
            return
        }
        unit = resources.displayMetrics.density * 0.55f
        measure()
        javi.x = (screenW / unit) * 0.5f
        addWindow()
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        if (Build.VERSION.SDK_INT >= 33) {
            registerReceiver(screenReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(screenReceiver, filter)
        }
        running = true
        resumeLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_FEED -> javi.feed()
            ACTION_PET -> javi.pet()
            ACTION_PLAY -> javi.play()
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (view != null) {
            measure()
            place()
        }
    }

    override fun onDestroy() {
        running = false
        active = false
        Choreographer.getInstance().removeFrameCallback(frame)
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // no estaba registrado
        }
        view?.let {
            try {
                wm.removeView(it)
            } catch (e: Exception) {
                // ya no estaba
            }
        }
        view = null
        super.onDestroy()
    }

    private fun resumeLoop() {
        active = true
        lastNs = 0L
        Choreographer.getInstance().removeFrameCallback(frame)
        Choreographer.getInstance().postFrameCallback(frame)
    }

    private fun measure() {
        val h: Int
        val nav: Int
        if (Build.VERSION.SDK_INT >= 30) {
            val m = wm.currentWindowMetrics
            screenW = m.bounds.width()
            h = m.bounds.height()
            nav = m.windowInsets
                .getInsetsIgnoringVisibility(WindowInsets.Type.navigationBars()).bottom
        } else {
            val pt = Point()
            @Suppress("DEPRECATION")
            wm.defaultDisplay.getRealSize(pt)
            screenW = pt.x
            h = pt.y
            val id = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            nav = if (id > 0) resources.getDimensionPixelSize(id) else 0
        }
        floorY = h - nav
        javi.setBounds(Javi.HALF_W, screenW / unit - Javi.HALF_W)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addWindow() {
        val v = PetView(this, javi, unit, true)
        val params = WindowManager.LayoutParams(
            (Javi.HALF_W * 2f * unit).toInt(),
            ((Javi.TOP + Javi.BOTTOM) * unit).toInt(),
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.START
        if (Build.VERSION.SDK_INT >= 28) {
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        lp = params
        view = v

        val slop = ViewConfiguration.get(this).scaledTouchSlop
        var downX = 0f
        var downY = 0f
        var dragging = false
        v.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX
                    downY = e.rawY
                    dragging = false
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!dragging &&
                        (abs(e.rawX - downX) > slop || abs(e.rawY - downY) > slop)
                    ) {
                        dragging = true
                        javi.hold()
                    }
                    if (dragging) {
                        javi.x = e.rawX / unit
                        javi.alt = max(0f, (floorY - e.rawY) / unit - 45f)
                        javi.setBounds(Javi.HALF_W, screenW / unit - Javi.HALF_W)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    if (dragging) {
                        javi.drop()
                    } else {
                        javi.pet()
                        Store.addMimo(this)
                    }
                    dragging = false
                }
            }
            true
        }

        setPosition(params)
        wm.addView(v, params)
    }

    private fun setPosition(params: WindowManager.LayoutParams): Boolean {
        val nx = ((javi.x - Javi.HALF_W) * unit).toInt()
        val ny = (floorY - (Javi.TOP + javi.alt) * unit).toInt()
        if (nx == params.x && ny == params.y) return false
        params.x = nx
        params.y = ny
        return true
    }

    private fun place() {
        val v = view ?: return
        val params = lp ?: return
        if (setPosition(params)) {
            try {
                wm.updateViewLayout(v, params)
            } catch (e: Exception) {
                // la ventana ya no existe
            }
        }
    }

    private fun goForeground() {
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ch = NotificationChannel(CHANNEL, "Javi", NotificationManager.IMPORTANCE_MIN)
        ch.setShowBadge(false)
        nm.createNotificationChannel(ch)

        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1,
            Intent(this, PetService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        val n = Notification.Builder(this, CHANNEL)
            .setSmallIcon(R.drawable.ic_paw)
            .setContentTitle("Javi está con vos")
            .setContentText("Tocá para abrir su casita")
            .setContentIntent(open)
            .setOngoing(true)
            .addAction(
                Notification.Action.Builder(
                    Icon.createWithResource(this, R.drawable.ic_paw), "Guardar a Javi", stop
                ).build()
            )
            .build()

        if (Build.VERSION.SDK_INT >= 34) {
            startForeground(1, n, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        } else {
            startForeground(1, n)
        }
    }
}
