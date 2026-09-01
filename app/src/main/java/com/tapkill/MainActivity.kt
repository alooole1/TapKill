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
        private const val ACCESSIBILITY_PERMISSION_REQUEST = 101
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
                // طلب الإذن
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:$packageName")
                )
                startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST)
                return
            }
        }
        
        // 2. بدء الخدمة
        startFloatingButtonService()
    }
    
    private fun startFloatingButtonService() {
        val intent = Intent(this, FloatingButtonService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
        Toast.makeText(this, "✅ تم تفعيل الزر العائم", Toast.LENGTH_SHORT).show()
        finish()
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            OVERLAY_PERMISSION_REQUEST -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (Settings.canDrawOverlays(this)) {
                        Toast.makeText(this, "✅ تم منح الإذن", Toast.LENGTH_SHORT).show()
                        startFloatingButtonService()
                    } else {
                        Toast.makeText(this, "❌ يجب منح إذن الرسم فوق التطبيقات", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
}
