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
import android.graphics.Rect
import android.graphics.drawable.Icon
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.SystemClock
import android.provider.Settings
import android.view.Choreographer
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowInsets
import android.view.WindowManager
import java.util.Calendar
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import kotlin.random.Random

/** Mantiene a Javi caminando sobre la pantalla, por encima de las demás apps. */
class PetService : Service() {

    companion object {
        @Volatile
        var running = false

        @Volatile
        var instance: PetService? = null

        const val ACTION_FEED = "com.isebx.javi.FEED"
        const val ACTION_PET = "com.isebx.javi.PET"
        const val ACTION_PLAY = "com.isebx.javi.PLAY"
        const val ACTION_STOP = "com.isebx.javi.STOP"
        const val ACTION_REFRESH = "com.isebx.javi.REFRESH"
        const val EXTRA_KIND = "kind"
        private const val CHANNEL = "javi"

        private const val MENU_MAIN = 0
        private const val MENU_FOOD = 1
        private const val MENU_SIZE = 2
        private const val MENU_PLAY = 3
    }

    private lateinit var wm: WindowManager
    private val handler = Handler(Looper.getMainLooper())

    private val javi = Javi()
    private var view: PetView? = null
    private var lp: WindowManager.LayoutParams? = null

    private val yarn = Yarn()
    private var yarnView: YarnView? = null
    private var yarnLp: WindowManager.LayoutParams? = null
    private var kicked = false

    private val fly = Firefly()
    private var flyView: FireflyView? = null
    private var flyLp: WindowManager.LayoutParams? = null
    private var flyTimer = 25f

    private var giftView: GiftView? = null
    private var giftLife = 0f
    private var giftTimer = 100f

    private var wand: WandView? = null

    private var stats: StatsView? = null
    private var careAcc = 0f
    private var saveAcc = 0f

    private var poopView: PoopView? = null
    private var poopX = 0f
    private var poopAge = 0f
    private var poopNagged = false

    private var wheel: WheelView? = null
    private var bath: BathView? = null
    private var menuLevel = MENU_MAIN

    private var unit = 1f
    private var screenW = 0
    private var floorY = 0
    private var lastNs = 0L
    private var active = true
    private var nightCheck = 0f
    private var iconTimer = 12f
    private var perchRect: Rect? = null
    private var lastTapReact = 0L

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
            stepYarn(dt)
            stepIcons(dt)
            stepFly(dt)
            stepGift(dt)
            stepCare(dt)
            wand?.let {
                it.step(dt)
                javi.attend(it.tipX / unit, (floorY - it.tipY) / unit, maxClimb(), true, true, true)
                it.invalidate()
            }
            bath?.let {
                it.step(dt)
                it.invalidate()
            }
            javi.update(dt)
            if (javi.gone) {
                stopSelf()
                return
            }
            place()
            view?.invalidate()
            if (javi.calm && !yarn.active && !fly.active && wand == null) {
                Choreographer.getInstance().postFrameCallbackDelayed(this, 60)
            } else {
                Choreographer.getInstance().postFrameCallback(this)
            }
        }
    }

    private var sensors: SensorManager? = null
    private var accel: Sensor? = null

    /** Si sacuden el teléfono, Javi se agarra con las uñas. */
    private val shake = object : SensorEventListener {
        override fun onSensorChanged(e: SensorEvent) {
            val ax = e.values[0]
            val ay = e.values[1]
            val az = e.values[2]
            val g = sqrt(ax * ax + ay * ay + az * az) / SensorManager.GRAVITY_EARTH
            if (g > 2.2f && view != null && bath == null) javi.cling()
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // no hace falta
        }
    }

    private val screenReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                Intent.ACTION_SCREEN_OFF -> {
                    active = false
                    sensors?.unregisterListener(shake)
                    endBath()
                    endWand()
                    closeStats()
                    Care.save(this@PetService)
                    closeMenu()
                }
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
        unit = resources.displayMetrics.density * Store.size(this)
        Care.load(this)
        javi.dirt = Care.dirt
        javi.collar = Store.collar(this)
        sensors = getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        accel = sensors?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
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
        instance = this
        resumeLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_FEED -> feed(
                (intent?.getIntExtra(EXTRA_KIND, Props.POLLO) ?: Props.POLLO).coerceIn(0, Props.AGUA)
            )
            ACTION_PET -> {
                javi.pet()
                Care.loved(0.03f)
            }
            ACTION_PLAY -> throwYarn()
            ACTION_REFRESH -> javi.collar = Store.collar(this)
            ACTION_STOP -> stopSelf()
        }
        return START_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (view != null) {
            closeMenu()
            endBath()
            measure()
            place()
        }
    }

    override fun onDestroy() {
        running = false
        instance = null
        active = false
        handler.removeCallbacksAndMessages(null)
        sensors?.unregisterListener(shake)
        Choreographer.getInstance().removeFrameCallback(frame)
        try {
            unregisterReceiver(screenReceiver)
        } catch (e: Exception) {
            // no estaba registrado
        }
        closeMenu()
        endBath()
        endWand()
        closeStats()
        removeGift()
        removePoop()
        Care.save(this)
        removeYarnWindow()
        removeFlyWindow()
        removeQuietly(view)
        view = null
        super.onDestroy()
    }

    private fun removeQuietly(v: View?) {
        if (v == null) return
        try {
            wm.removeView(v)
        } catch (e: Exception) {
            // ya no estaba
        }
    }

    private fun resumeLoop() {
        active = true
        sensors?.unregisterListener(shake)
        accel?.let { sensors?.registerListener(shake, it, SensorManager.SENSOR_DELAY_UI) }
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

    private fun overlayParams(w: Int, h: Int): WindowManager.LayoutParams {
        val params = WindowManager.LayoutParams(
            w, h,
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
        return params
    }

    // ---------- Javi ----------

    @SuppressLint("ClickableViewAccessibility")
    private fun addWindow() {
        val v = PetView(this, javi, unit, true)
        val params = overlayParams(
            (Javi.HALF_W * 2f * unit).toInt(),
            ((Javi.TOP + Javi.BOTTOM) * unit).toInt()
        )
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        lp = params
        view = v

        val slop = ViewConfiguration.get(this).scaledTouchSlop
        var downX = 0f
        var downY = 0f
        var dragging = false
        var rubbing = false
        var longFired = false
        val longPress = Runnable {
            longFired = true
            openMenu()
        }
        v.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_OUTSIDE -> onOutsideTouch()
                MotionEvent.ACTION_DOWN -> {
                    downX = e.rawX
                    downY = e.rawY
                    dragging = false
                    rubbing = false
                    longFired = false
                    handler.postDelayed(longPress, 450L)
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!dragging && !rubbing && !longFired &&
                        (abs(e.rawX - downX) > slop || abs(e.rawY - downY) > slop)
                    ) {
                        handler.removeCallbacks(longPress)
                        // De lado a lado es una caricia; hacia arriba o abajo lo levanta.
                        if (abs(e.rawX - downX) > abs(e.rawY - downY) * 1.5f) {
                            rubbing = true
                            Store.addMimo(this)
                        } else {
                            dragging = true
                            perchRect = null
                            javi.hold()
                        }
                    }
                    if (rubbing) {
                        javi.rub(e.rawX / unit)
                    }
                    if (dragging) {
                        javi.x = e.rawX / unit
                        javi.alt = max(0f, (floorY - e.rawY) / unit - 45f)
                        javi.setBounds(Javi.HALF_W, screenW / unit - Javi.HALF_W)
                    }
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    handler.removeCallbacks(longPress)
                    if (dragging) {
                        // Si lo sueltan en lo alto (sobre un ícono), se queda colgado un rato.
                        if (javi.alt > 70f) release(e.rawX, e.rawY) else javi.drop()
                    } else if (rubbing) {
                        rubbing = false
                        Care.loved(0.05f)
                    } else if (!longFired && e.actionMasked == MotionEvent.ACTION_UP) {
                        javi.pet()
                        Store.addMimo(this)
                        Care.loved(0.03f)
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

    /** Cambia el tamaño de Javi y vuelve a crear sus ventanas. */
    private fun resize(delta: Float) {
        val old = unit
        Store.setSize(this, Store.size(this) + delta)
        val next = resources.displayMetrics.density * Store.size(this)
        if (abs(next - old) < 0.001f) return
        val hadYarn = yarn.active
        removeYarnWindow()
        fly.active = false
        removeFlyWindow()
        removeGift()
        removePoop()
        removeQuietly(view)
        view = null
        javi.x = javi.x * old / next
        yarn.x = yarn.x * old / next
        unit = next
        measure()
        addWindow()
        if (hadYarn) addYarnWindow()
    }

    // ---------- Íconos y toques en la pantalla ----------

    /** Cada tanto, si ve íconos, salta a sentarse sobre uno o a colgarse. */
    private fun stepIcons(dt: Float) {
        val target = perchRect
        if (target != null) {
            val up = javi.perched || javi.st == Javi.St.JUMPTO
            if (!up) {
                perchRect = null
            } else if (!JaviEyes.icons.contains(target)) {
                perchRect = null
                javi.drop()
            }
            return
        }
        iconTimer -= dt
        if (iconTimer > 0f) return
        iconTimer = 25f + Random.nextFloat() * 30f
        if (!javi.idle || yarn.active || wheel != null) return
        val list = JaviEyes.icons
        if (list.isEmpty()) return
        val r = list[Random.nextInt(list.size)]
        val grip = (floorY - (r.top + r.height() * 0.05f)) / unit
        val maxAlt = floorY * 0.82f / unit
        if (grip < 40f || grip > maxAlt) return
        val hang = Random.nextBoolean() && grip - Javi.HANG_LEN > 25f
        val tx = r.exactCenterX() / unit
        perchRect = r
        if (hang) {
            javi.jumpTo(tx + 8f * (if (tx >= javi.x) 1f else -1f), grip - Javi.HANG_LEN, true)
        } else {
            javi.jumpTo(tx, grip, false)
        }
    }

    /**
     * Lo soltaron en lo alto. Si Javi ve los íconos y el dedo quedó sobre uno,
     * se cuelga de ahí; si no hay de dónde agarrarse, aguanta un instante y
     * resbala con las uñas hasta el piso.
     */
    private fun release(px: Float, py: Float) {
        if (JaviEyes.connected) {
            val pad = (10f * unit).toInt()
            for (r in JaviEyes.icons) {
                if (px >= r.left - pad && px <= r.right + pad &&
                    py >= r.top - pad && py <= r.bottom + pad
                ) {
                    val grip = (floorY - (r.top + r.height() * 0.05f)) / unit
                    if (grip - Javi.HANG_LEN > 10f) {
                        perchRect = r
                        javi.grabAt(r.exactCenterX() / unit, grip - Javi.HANG_LEN)
                        return
                    }
                }
            }
        }
        javi.grabAndSlip()
    }

    /** JaviEyes avisa que tocaron algo en la pantalla (coordenadas en píxeles). */
    fun onScreenTap(px: Float, py: Float) {
        handler.post {
            val now = SystemClock.uptimeMillis()
            if (view == null || wheel != null || yarn.active) return@post
            if (now - lastTapReact < 7000L || !javi.idle) return@post
            if (py < 0f) return@post
            val r = Random.nextFloat()
            if (r < 0.45f) {
                lastTapReact = now
                javi.goTo(px / unit, Javi.St.POUNCE)
            } else if (r < 0.75f) {
                lastTapReact = now
                javi.goTo(px / unit, Javi.St.BEG)
            }
        }
    }

    /**
     * Tocaron la pantalla fuera de Javi. Android no dice dónde, solo que pasó:
     * cada tanto se acerca al medio a pedir caricias o pega un saltito.
     */
    private fun onOutsideTouch() {
        if (JaviEyes.connected) return
        val now = SystemClock.uptimeMillis()
        if (wheel != null || yarn.active || !javi.idle) return
        if (now - lastTapReact < 12000L) return
        val r = Random.nextFloat()
        val mid = screenW / unit * (0.3f + Random.nextFloat() * 0.4f)
        if (r < 0.18f) {
            lastTapReact = now
            javi.goTo(mid, Javi.St.BEG)
        } else if (r < 0.30f) {
            lastTapReact = now
            javi.goTo(mid, Javi.St.POUNCE)
        }
    }

    // ---------- Ovillo ----------

    private fun throwYarn() {
        val w = screenW / unit
        val sx = (javi.x + javi.dir * 70f).coerceIn(Yarn.R + 4f, w - Yarn.R - 4f)
        yarn.spawn(sx, 150f, javi.dir * 90f, 80f)
        if (yarnView == null) addYarnWindow()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun addYarnWindow() {
        val v = YarnView(this, yarn, unit)
        val side = (YarnView.HALF * 2f * unit).toInt()
        val params = overlayParams(side, side)
        yarnLp = params
        yarnView = v

        var lastX = 0f
        var lastY = 0f
        var lastT = 0L
        var vx = 0f
        var vy = 0f
        v.setOnTouchListener { _, e ->
            when (e.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    yarn.held = true
                    yarn.life = Yarn.LIFE
                    lastX = e.rawX
                    lastY = e.rawY
                    lastT = SystemClock.uptimeMillis()
                    vx = 0f
                    vy = 0f
                }
                MotionEvent.ACTION_MOVE -> {
                    val now = SystemClock.uptimeMillis()
                    val dt = max(1L, now - lastT) / 1000f
                    vx = (e.rawX - lastX) / unit / dt
                    vy = (lastY - e.rawY) / unit / dt
                    lastX = e.rawX
                    lastY = e.rawY
                    lastT = now
                    yarn.x = e.rawX / unit
                    yarn.alt = max(0f, (floorY - e.rawY) / unit - Yarn.R)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    yarn.held = false
                    yarn.vx = vx.coerceIn(-900f, 900f)
                    yarn.vy = vy.coerceIn(-900f, 900f)
                    yarn.life = Yarn.LIFE
                }
            }
            true
        }
        placeYarn(params)
        wm.addView(v, params)
    }

    private fun placeYarn(params: WindowManager.LayoutParams): Boolean {
        val nx = ((yarn.x - YarnView.HALF) * unit).toInt()
        val ny = (floorY - (yarn.alt + Yarn.R + YarnView.HALF) * unit).toInt()
        if (nx == params.x && ny == params.y) return false
        params.x = nx
        params.y = ny
        return true
    }

    private fun removeYarnWindow() {
        removeQuietly(yarnView)
        yarnView = null
        yarnLp = null
    }

    private fun stepYarn(dt: Float) {
        if (!yarn.active) {
            if (yarnView != null) removeYarnWindow()
            return
        }
        val w = screenW / unit
        yarn.update(dt, Yarn.R + 2f, w - Yarn.R - 2f)
        if (!yarn.active) {
            removeYarnWindow()
            return
        }
        if (!yarn.held) {
            javi.chase(yarn.x)
            if (javi.leaping) {
                if (!kicked && abs(yarn.x - javi.x) < 65f && yarn.alt < 85f) {
                    yarn.kick(javi.dir)
                    kicked = true
                    Care.played(0.05f)
                }
            } else {
                kicked = false
            }
        }
        val v = yarnView ?: return
        val params = yarnLp ?: return
        if (placeYarn(params)) {
            try {
                wm.updateViewLayout(v, params)
            } catch (e: Exception) {
                // la ventana ya no existe
            }
        }
        v.invalidate()
    }

    // ---------- Rueda de acciones ----------

    private fun openMenu() {
        if (wheel != null) return
        val d = resources.displayMetrics.density
        val w = WheelView(this, d)
        w.cx = javi.x * unit
        w.cy = floorY - 60f * unit
        w.onPick = { i -> onPick(i) }
        w.onDismiss = { closeMenu() }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT
        )
        params.dimAmount = 0.4f
        params.gravity = Gravity.TOP or Gravity.START
        if (Build.VERSION.SDK_INT >= 28) {
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        wheel = w
        showLevel(MENU_MAIN)
        try {
            wm.addView(w, params)
        } catch (e: Exception) {
            wheel = null
        }
    }

    private fun showLevel(level: Int) {
        menuLevel = level
        val w = wheel ?: return
        when (level) {
            MENU_MAIN -> w.show(
                intArrayOf(
                    Props.POLLO, Props.AGUA, Props.YARN, Props.SHOWER, Props.STATS,
                    Props.SIZE, Props.HOME
                ),
                arrayOf("Comer", "Agua", "Jugar", "Bañar", "Cómo está", "Tamaño", "A casa")
            )
            MENU_FOOD -> w.show(
                intArrayOf(Props.POLLO, Props.CARNE, Props.PESCADO, Props.LECHE, Props.ZAPALLO),
                Props.FOOD_NAMES
            )
            MENU_PLAY -> w.show(
                intArrayOf(Props.YARN, Props.FIREFLY, Props.BUTTERFLY, Props.WAND, Props.BOX),
                arrayOf("Ovillo", "Luciérnaga", "Mariposa", "Plumero", "Cajita")
            )
            MENU_SIZE -> w.show(
                intArrayOf(Props.MINUS, Props.OK, Props.PLUS),
                arrayOf("Más chico", "Listo", "Más grande")
            )
        }
    }

    private fun onPick(i: Int) {
        when (menuLevel) {
            MENU_MAIN -> when (i) {
                0 -> showLevel(MENU_FOOD)
                1 -> {
                    closeMenu()
                    feed(Props.AGUA)
                }
                2 -> showLevel(MENU_PLAY)
                3 -> {
                    closeMenu()
                    startBath()
                }
                4 -> {
                    closeMenu()
                    openStats()
                }
                5 -> showLevel(MENU_SIZE)
                6 -> {
                    closeMenu()
                    yarn.active = false
                    javi.leave()
                }
            }
            MENU_FOOD -> {
                closeMenu()
                feed(i)
                Store.addComida(this)
            }
            MENU_PLAY -> {
                closeMenu()
                when (i) {
                    0 -> throwYarn()
                    1 -> releaseFly(Firefly.FIREFLY)
                    2 -> releaseFly(Firefly.BUTTERFLY)
                    3 -> startWand()
                    4 -> javi.startBox()
                }
            }
            MENU_SIZE -> when (i) {
                0 -> resize(-Store.SIZE_STEP)
                1 -> closeMenu()
                2 -> resize(Store.SIZE_STEP)
            }
        }
    }

    private fun closeMenu() {
        removeQuietly(wheel)
        wheel = null
    }

    // ---------- Luciérnaga ----------

    private fun maxClimb(): Float = floorY * 0.8f / unit - Javi.HANG_LEN

    private fun releaseFly(kind: Int) {
        if (fly.active || bath != null || wand != null) return
        val w = screenW / unit
        fly.spawn(0f, w, floorY * 0.85f / unit, kind)
        if (flyView == null) addFlyWindow()
    }

    /** De día pasa una mariposa; de noche, una luciérnaga. */
    private fun timeKind(): Int {
        val h = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return if (h in 7..18) Firefly.BUTTERFLY else Firefly.FIREFLY
    }

    private fun addFlyWindow() {
        val v = FireflyView(this, fly, unit)
        val side = (FireflyView.HALF * 2f * unit).toInt()
        val params = overlayParams(side, side)
        // No se toca, y con esta transparencia Android deja pasar los toques a lo de abajo.
        params.flags = params.flags or WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        params.alpha = 0.8f
        flyLp = params
        flyView = v
        placeFly(params)
        try {
            wm.addView(v, params)
        } catch (e: Exception) {
            flyView = null
            flyLp = null
            fly.active = false
        }
    }

    private fun placeFly(params: WindowManager.LayoutParams): Boolean {
        val nx = ((fly.x - FireflyView.HALF) * unit).toInt()
        val ny = (floorY - (fly.alt + FireflyView.HALF) * unit).toInt()
        if (nx == params.x && ny == params.y) return false
        params.x = nx
        params.y = ny
        return true
    }

    private fun removeFlyWindow() {
        removeQuietly(flyView)
        flyView = null
        flyLp = null
    }

    /** Cada tanto pasa sola una luciérnaga, más seguido de noche. */
    private fun stepFly(dt: Float) {
        if (!fly.active) {
            if (flyView != null) removeFlyWindow()
            flyTimer -= dt
            if (flyTimer <= 0f) {
                flyTimer = if (javi.night) 45f + Random.nextFloat() * 50f
                else 80f + Random.nextFloat() * 90f
                if (javi.idle && !yarn.active && wheel == null && bath == null) releaseFly(timeKind())
            }
            return
        }
        if (fly.kind == Firefly.BUTTERFLY) {
            val host = javi.st == Javi.St.WATCH || javi.st == Javi.St.STARE
            val ev = fly.updateButterfly(dt, host, javi.noseX, javi.noseAlt)
            if (ev == 1 && !javi.stare()) fly.updateButterfly(0f, false, javi.noseX, javi.noseAlt)
            if (ev == 2) javi.endStare()
        } else {
            fly.update(dt, javi.x, javi.pawAlt, javi.swatting)
        }
        if (!fly.active || bath != null || wand != null) {
            fly.active = false
            javi.endStare()
            removeFlyWindow()
            return
        }
        if (!yarn.active && !fly.landed) {
            if (fly.kind == Firefly.BUTTERFLY) {
                javi.attend(fly.x, fly.alt, maxClimb(), false, false, false)
            } else {
                javi.attend(fly.x, fly.alt, maxClimb())
            }
        }
        val v = flyView ?: return
        val params = flyLp ?: return
        if (placeFly(params)) {
            try {
                wm.updateViewLayout(v, params)
            } catch (e: Exception) {
                // la ventana ya no existe
            }
        }
        v.invalidate()
    }

    // ---------- Cuidado: comida, diversión, limpieza y caca ----------

    private fun feed(kind: Int) {
        javi.feed(kind)
        if (kind == Props.AGUA) Care.drink() else Care.feed(kind == Props.LECHE)
    }

    private val quiet: Boolean
        get() = wheel == null && bath == null && wand == null && stats == null

    @SuppressLint("ClickableViewAccessibility")
    private fun stepCare(dt: Float) {
        careAcc += dt
        if (careAcc >= 1f) {
            Care.tick(careAcc)
            Care.tickAwake(careAcc)
            careAcc = 0f
            if (bath == null) javi.dirt = Care.dirt
            javi.needs = Care.want
            stats?.invalidate()
        }
        saveAcc += dt
        if (saveAcc >= 30f) {
            saveAcc = 0f
            Care.save(this)
        }
        if (javi.takeGroomed()) Care.groomed()
        if (javi.swatting || wand != null) Care.played(dt * 0.012f)

        // Hora de hacer caca
        if (Care.poopDue && poopView == null && quiet && !yarn.active && !fly.active) {
            if (javi.startPoop()) Care.pooped()
        }
        when (javi.takePoopEvent()) {
            1 -> {
                // Acaba de hacer y ya se dio vuelta: queda delante suyo
                poopX = javi.x + javi.dir * 46f
                val v = PoopView(this, unit)
                val side = (PoopView.HALF * 2f * unit).toInt()
                val params = overlayParams(side, side)
                params.x = ((poopX - PoopView.HALF) * unit).toInt()
                params.y = floorY - side
                v.setOnTouchListener { _, e ->
                    if (e.actionMasked == MotionEvent.ACTION_UP) {
                        Care.poopCleaned()
                        removePoop()
                        javi.pet()
                    }
                    true
                }
                try {
                    wm.addView(v, params)
                    poopView = v
                    poopAge = 0f
                    poopNagged = false
                } catch (e: Exception) {
                    poopView = null
                }
            }
            2 -> poopView?.let {
                it.covered = 1f
                it.invalidate()
            }
        }
        val pv = poopView ?: return
        if (javi.st == Javi.St.COVER) {
            pv.covered = javi.coverProgress
            pv.invalidate()
        }
        poopAge += dt
        // Si pasa mucho sin juntarla, va, la huele y te mira
        if (!poopNagged && poopAge > 300f && javi.idle && quiet) {
            poopNagged = true
            Care.poopLeft()
            javi.goTo(poopX - 50f, Javi.St.BEG)
        }
    }

    private fun removePoop() {
        removeQuietly(poopView)
        poopView = null
    }

    private fun openStats() {
        if (stats != null) return
        val v = StatsView(this, resources.displayMetrics.density)
        v.onExit = { handler.post { closeStats() } }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT
        )
        params.dimAmount = 0.5f
        params.gravity = Gravity.TOP or Gravity.START
        try {
            wm.addView(v, params)
            stats = v
        } catch (e: Exception) {
            stats = null
        }
    }

    private fun closeStats() {
        removeQuietly(stats)
        stats = null
    }

    // ---------- Regalito ----------

    /** Cada tanto Javi trae una flor y la deja en el piso; se toca para aceptarla. */
    @SuppressLint("ClickableViewAccessibility")
    private fun stepGift(dt: Float) {
        if (giftView != null) {
            giftLife -= dt
            if (giftLife <= 0f) removeGift()
        }
        if (javi.takeGift() && giftView == null) {
            val v = GiftView(this, unit)
            val side = (GiftView.HALF * 2f * unit).toInt()
            val params = overlayParams(side, side)
            params.x = ((javi.x + javi.dir * 66f - GiftView.HALF) * unit).toInt()
            params.y = floorY - side
            v.setOnTouchListener { _, e ->
                if (e.actionMasked == MotionEvent.ACTION_UP) {
                    Store.addRegalo(this)
                    javi.pet()
                    removeGift()
                }
                true
            }
            try {
                wm.addView(v, params)
                giftView = v
                giftLife = 35f
            } catch (e: Exception) {
                giftView = null
            }
        }
        giftTimer -= dt
        if (giftTimer <= 0f) {
            // Si está bien cuidado, trae regalitos más seguido
            giftTimer = (180f + Random.nextFloat() * 240f) * (if (Care.happy) 0.6f else 1f)
            if (javi.idle && giftView == null && !yarn.active && !fly.active &&
                wheel == null && bath == null && wand == null
            ) {
                val w = screenW / unit
                javi.startGift(w * (0.3f + Random.nextFloat() * 0.4f))
            }
        }
    }

    private fun removeGift() {
        removeQuietly(giftView)
        giftView = null
    }

    // ---------- Plumero ----------

    private fun startWand() {
        if (wand != null || bath != null) return
        yarn.active = false
        fly.active = false
        val w = WandView(this, resources.displayMetrics.density)
        w.onExit = { handler.post { endWand() } }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
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
        try {
            wm.addView(w, params)
            wand = w
        } catch (e: Exception) {
            wand = null
        }
    }

    private fun endWand() {
        val w = wand ?: return
        wand = null
        removeQuietly(w)
    }

    // ---------- Baño ----------

    private fun startBath() {
        if (bath != null) return
        endWand()
        yarn.active = false
        perchRect = null
        javi.startBath()
        val b = BathView(this, javi, resources.displayMetrics.density)
        b.onExit = { handler.post { endBath() } }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_DIM_BEHIND,
            PixelFormat.TRANSLUCENT
        )
        params.dimAmount = 0.72f
        params.gravity = Gravity.TOP or Gravity.START
        if (Build.VERSION.SDK_INT >= 28) {
            params.layoutInDisplayCutoutMode =
                WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
        }
        try {
            wm.addView(b, params)
            bath = b
            view?.visibility = View.INVISIBLE
        } catch (e: Exception) {
            javi.endBath()
        }
    }

    private fun endBath() {
        val b = bath ?: return
        bath = null
        Care.bathed(b.quality)
        javi.dirt = Care.dirt
        removeQuietly(b)
        view?.visibility = View.VISIBLE
        javi.endBath()
    }

    // ---------- Notificación ----------

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
