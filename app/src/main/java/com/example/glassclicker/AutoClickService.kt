package com.example.glassclicker

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Path
import android.graphics.PixelFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Button
import android.widget.Toast

class AutoClickService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var running = false
    private val handler = Handler(Looper.getMainLooper())

    private val tick = object : Runnable {
        override fun run() {
            if (!running) return
            performCycle()
            val interval = prefs().getInt(MainActivity.KEY_INTERVAL, 1000).coerceAtLeast(1000)
            handler.postDelayed(this, interval.toLong())
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        showPanel()
    }

    private fun prefs() = getSharedPreferences(MainActivity.PREFS, Context.MODE_PRIVATE)

    @SuppressLint("ClickableViewAccessibility")
    private fun showPanel() {
        if (floatingView != null) return

        val wm = getSystemService(WINDOW_SERVICE) as WindowManager
        val view = LayoutInflater.from(this).inflate(R.layout.widget_layout, null)

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 200
        }

        val handle = view.findViewById<View>(R.id.drag_handle)
        var startX = 0; var startY = 0
        var touchX = 0f; var touchY = 0f
        handle.setOnTouchListener { _, e ->
            when (e.action) {
                MotionEvent.ACTION_DOWN -> {
                    startX = params.x; startY = params.y
                    touchX = e.rawX; touchY = e.rawY
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    params.x = startX + (e.rawX - touchX).toInt()
                    params.y = startY + (e.rawY - touchY).toInt()
                    wm.updateViewLayout(view, params)
                    true
                }
                else -> false
            }
        }

        val btn = view.findViewById<Button>(R.id.btn_start)
        btn.setOnClickListener {
            if (running) {
                stopClicking()
                btn.text = "▶ СТАРТ"
            } else {
                if (prefs().getInt(MainActivity.KEY_BTN_X, -1) < 0) {
                    Toast.makeText(this, "Сначала задай координаты в приложении", Toast.LENGTH_LONG).show()
                    return@setOnClickListener
                }
                running = true
                btn.text = "⏹ СТОП"
                handler.post(tick)
            }
        }

        wm.addView(view, params)
        windowManager = wm
        floatingView = view
    }

    private fun stopClicking() {
        running = false
        handler.removeCallbacksAndMessages(null)
    }

    private fun performCycle() {
        val p = prefs()
        val text = p.getString(MainActivity.KEY_TEXT, "") ?: ""
        val fx = p.getInt(MainActivity.KEY_FIELD_X, -1)
        val fy = p.getInt(MainActivity.KEY_FIELD_Y, -1)
        val bx = p.getInt(MainActivity.KEY_BTN_X, -1)
        val by = p.getInt(MainActivity.KEY_BTN_Y, -1)

        if (text.isNotEmpty() && fx >= 0 && fy >= 0) {
            tap(fx.toFloat(), fy.toFloat())
            handler.postDelayed({ if (running) setFocusedText(text) }, 400)
            handler.postDelayed({ if (running) tap(bx.toFloat(), by.toFloat()) }, 800)
        } else {
            tap(bx.toFloat(), by.toFloat())
        }
    }

    private fun tap(x: Float, y: Float) {
        val path = Path().apply { moveTo(x, y) }
        val stroke = GestureDescription.StrokeDescription(path, 0, 50)
        dispatchGesture(GestureDescription.Builder().addStroke(stroke).build(), null, null)
    }

    private fun setFocusedText(text: String) {
        val node = findFocus(AccessibilityNodeInfo.FOCUS_INPUT) ?: return
        val args = Bundle().apply {
            putCharSequence(AccessibilityNodeInfo.ACTION_ARGUMENT_SET_TEXT_CHARSEQUENCE, text)
        }
        node.performAction(AccessibilityNodeInfo.ACTION_SET_TEXT, args)
    }

    private fun removePanel() {
        stopClicking()
        floatingView?.let { windowManager?.removeView(it) }
        floatingView = null
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {}
    override fun onInterrupt() { stopClicking() }

    override fun onUnbind(intent: android.content.Intent?): Boolean {
        removePanel()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        removePanel()
        super.onDestroy()
    }
}
