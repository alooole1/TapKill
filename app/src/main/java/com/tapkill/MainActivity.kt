package com.tapkill

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    
    companion object {
        private const val OVERLAY_PERMISSION_REQUEST = 100
    }
    
    private lateinit var btnStartService: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        btnStartService = findViewById(R.id.btnStartService)
        btnStartService.setOnClickListener {
            checkPermissionsAndStart()
        }
    }
    
    private fun checkPermissionsAndStart() {
        // 1. التحقق من صلاحية الرسم فوق التطبيقات
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST)
                return
            }
        }
        
        // 2. فتح إعدادات الإمكانية لتفعيل الخدمة
        Toast.makeText(this, "⚠️ افتح إعدادات الإمكانية وفعّل TapKill", Toast.LENGTH_LONG).show()
        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        startActivity(intent)
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            OVERLAY_PERMISSION_REQUEST -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, "✅ تم منح الإذن", Toast.LENGTH_SHORT).show()
                        // فتح إعدادات الإمكانية
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "❌ يجب منح إذن الرسم فوق التطبيقات", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
