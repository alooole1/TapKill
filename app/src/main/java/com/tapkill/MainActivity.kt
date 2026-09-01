package com.tapkill

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    private lateinit var btnStartService: Button
    private var overlayPermissionRequested = false
    private var accessibilityRequested = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        btnStartService = findViewById(R.id.btnStartService)
        btnStartService.setOnClickListener {
            checkAndEnable()
        }
    }
    
    private fun checkAndEnable() {
        // 1. التحقق من صلاحية الرسم فوق التطبيقات
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                overlayPermissionRequested = true
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName")
                )
                startActivity(intent)
                return
            }
        }
        
        // 2. فتح إعدادات الإمكانية بشكل مباشر
        openAccessibilitySettings()
    }
    
    private fun openAccessibilitySettings() {
        try {
            // محاولة فتح إعدادات الإمكانية مباشرة
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            Toast.makeText(this, "⚠️ قم بتفعيل TapKill في الإعدادات", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            // إذا فشل، حاول فتح الإعدادات العامة
            try {
                val intent = Intent(Settings.ACTION_SETTINGS)
                startActivity(intent)
                Toast.makeText(this, "⚠️ اذهب إلى إمكانية الوصول وفعّل TapKill", Toast.LENGTH_LONG).show()
            } catch (e2: Exception) {
                Toast.makeText(this, "❌ لا يمكن فتح الإعدادات", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        
        // التحقق من حالة الأذونات عند العودة
        if (overlayPermissionRequested) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "✅ تم منح إذن الرسم", Toast.LENGTH_SHORT).show()
                    openAccessibilitySettings()
                }
            }
            overlayPermissionRequested = false
        }
        
        // التحقق من تفعيل خدمة الإمكانية
        checkAccessibilityStatus()
    }
    
    private fun checkAccessibilityStatus() {
        try {
            val enabledServices = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
            )
            
            if (enabledServices?.contains("com.tapkill") == true) {
                Toast.makeText(this, "✅ خدمة TapKill مفعلة!", Toast.LENGTH_SHORT).show()
                // بدء الخدمة
                startFloatingService()
            } else {
                // عدم التفعيل - محاولة مرة أخرى
                if (!accessibilityRequested) {
                    accessibilityRequested = true
                    openAccessibilitySettings()
                }
            }
        } catch (e: Exception) {
            // فشل في القراءة
        }
    }
    
    private fun startFloatingService() {
        val intent = Intent(this, FloatingButtonService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "🚀 تم تشغيل الزر العائم", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (Settings.canDrawOverlays(this)) {
                    Toast.makeText(this, "✅ تم منح الإذن", Toast.LENGTH_SHORT).show()
                    openAccessibilitySettings()
                }
            }
        }
    }
}
