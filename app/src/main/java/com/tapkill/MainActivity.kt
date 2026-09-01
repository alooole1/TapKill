package com.tapkill

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    companion object {
        private const val TAG = "TapKill"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: بدء التشغيل")
        
        try {
            setContentView(R.layout.activity_main)
            Log.d(TAG, "onCreate: setContentView تم بنجاح")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: فشل setContentView", e)
        }
    }
}
