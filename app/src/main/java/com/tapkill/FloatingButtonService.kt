package com.tapkill

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.ImageButton
import android.widget.Toast

class FloatingButtonService : Service() {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingButton: View

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // تضخيم زر عائم من layout (سننشئه لاحقاً)
        floatingButton = LayoutInflater.from(this).inflate(R.layout.floating_button, null)
        val button = floatingButton.findViewById<ImageButton>(R.id.btnKill)

        button.setOnClickListener {
            // عند الضغط، سنقوم بتشغيل خدمة Accessibility لتنفيذ النقر على زر الإغلاق
            try {
                val intent = Intent(this, TapKillAccessibilityService::class.java)
                intent.putExtra("ACTION", "KILL_AD")
                startService(intent)
                Toast.makeText(this, "جاري تجاوز الإعلان...", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, "خطأ: تأكد من تفعيل خدمة الإمكانية", Toast.LENGTH_SHORT).show()
            }
        }

        // إعدادات النافذة العائمة
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

        windowManager.addView(floatingButton, params)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::floatingButton.isInitialized) {
            windowManager.removeView(floatingButton)
        }
    }
}
