package com.tapkill

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.Toast
import androidx.core.app.NotificationCompat
import kotlin.math.abs

class FloatingButtonService : Service() {
    
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    
    companion object {
        private const val CHANNEL_ID = "TapKillChannel"
        private const val NOTIFICATION_ID = 1
        private var isAccessibilityEnabled = false
    }
    
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startForeground(NOTIFICATION_ID, NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TapKill")
            .setContentText("الزر العائم نشط")
            .setSmallIcon(android.R.drawable.ic_menu_edit)
            .build())
        
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        inflateFloatingView()
        checkAccessibilityStatus()
    }
    
    private fun checkAccessibilityStatus() {
        try {
            val enabledServices = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            isAccessibilityEnabled = enabledServices?.contains("com.tapkill") == true
            updateButtonState()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun updateButtonState() {
        val btnKill = floatingView.findViewById<Button>(R.id.btnKill)
        if (isAccessibilityEnabled) {
            btnKill.text = "🔫"
            btnKill.setBackgroundColor(0xFFFF0000.toInt())
        } else {
            btnKill.text = "⚡"
            btnKill.setBackgroundColor(0xFF00FF00.toInt())
        }
    }
    
    private fun inflateFloatingView() {
        val inflater = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        floatingView = inflater.inflate(R.layout.floating_button, null)
        
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            else
                WindowManager.LayoutParams.TYPE_PHONE,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT
        )
        
        params.gravity = Gravity.TOP or Gravity.START
        params.x = 100
        params.y = 200
        
        windowManager.addView(floatingView, params)
        
        val btnKill = floatingView.findViewById<Button>(R.id.btnKill)
        btnKill.setOnClickListener {
            if (!isAccessibilityEnabled) {
                val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
                Toast.makeText(this, "⚠️ فعّل خدمة TapKill في الإعدادات", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "✅ الزر جاهز لإغلاق الإعلانات", Toast.LENGTH_SHORT).show()
            }
        }
        
        floatingView.setOnTouchListener(FloatingOnTouchListener(params, windowManager))
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "TapKill Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }
}

class FloatingOnTouchListener(
    private val params: WindowManager.LayoutParams,
    private val windowManager: WindowManager
) : View.OnTouchListener {
    
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var isDragging = false
    
    override fun onTouch(v: View, event: android.view.MotionEvent): Boolean {
        when (event.action) {
            android.view.MotionEvent.ACTION_DOWN -> {
                initialX = params.x
                initialY = params.y
                initialTouchX = event.rawX
                initialTouchY = event.rawY
                isDragging = false
                return true
            }
            android.view.MotionEvent.ACTION_MOVE -> {
                val deltaX = abs(event.rawX - initialTouchX)
                val deltaY = abs(event.rawY - initialTouchY)
                if (deltaX > 10 || deltaY > 10) {
                    isDragging = true
                }
                if (isDragging) {
                    params.x = initialX + (event.rawX - initialTouchX).toInt()
                    params.y = initialY + (event.rawY - initialTouchY).toInt()
                    windowManager.updateViewLayout(v, params)
                }
                return true
            }
            android.view.MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    v.performClick()
                }
                return true
            }
        }
        return false
    }
}
