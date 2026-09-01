package com.tapkill

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import kotlin.math.abs

class TapKillAccessibilityService : AccessibilityService() {
    
    companion object {
        private const val TAG = "TapKillAccessibility"
        private var instance: TapKillAccessibilityService? = null
        
        fun getInstance(): TapKillAccessibilityService? = instance
        fun isServiceRunning(): Boolean = instance != null
    }
    
    private val handler = Handler(Looper.getMainLooper())
    private var isProcessing = false
    private var lastClickTime = 0L
    private val CLICK_DELAY = 500L
    
    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
        Log.d(TAG, "✅ خدمة الإمكانية متصلة")
        
        val info = serviceInfo
        info.apply {
            eventTypes = AccessibilityEvent.TYPES_ALL_MASK
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                flags = flags or AccessibilityServiceInfo.FLAG_ENABLE_ACCESSIBILITY_VOLUME
            }
        }
        serviceInfo = info
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null || isProcessing) return
        
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastClickTime < CLICK_DELAY) return
        
        val packageName = event.packageName?.toString() ?: return
        
        val adPackages = listOf(
            "com.google.android.gms",
            "com.google.android.gms.ads",
            "com.facebook.ads",
            "com.unity3d.ads",
            "com.applovin",
            "com.adcolony",
            "com.chartboost",
            "com.google.ads",
            "com.inmobi",
            "com.mopub",
            "com.vungle",
            "com.ironsource",
            "com.tapjoy",
            "com.admob"
        )
        
        if (adPackages.any { packageName.contains(it, ignoreCase = true) }) {
            Log.d(TAG, "📢 تم اكتشاف إعلان من: $packageName")
            closeAd()
        }
    }
    
    private fun closeAd() {
        if (isProcessing) return
        isProcessing = true
        lastClickTime = System.currentTimeMillis()
        
        val root = rootInActiveWindow ?: run {
            isProcessing = false
            return
        }
        
        val closeTexts = listOf(
            "إغلاق", "إلغاء", "تخطي", "Skip", "Close", "X", "✕", 
            "×", "تجاهل", "Dismiss", "Cancel", "لا شكراً", "Not now"
        )
        
        findAndClickCloseButton(root, closeTexts)
        
        handler.postDelayed({
            if (isProcessing) {
                clickTopRightCorner()
            }
        }, 300)
    }
    
    private fun findAndClickCloseButton(node: android.view.accessibility.AccessibilityNodeInfo, closeTexts: List<String>) {
        try {
            for (i in 0 until node.childCount) {
                val child = node.getChild(i) ?: continue
                
                val text = child.text?.toString() ?: ""
                val contentDesc = child.contentDescription?.toString() ?: ""
                
                if (closeTexts.any { 
                    text.contains(it, ignoreCase = true) || 
                    contentDesc.contains(it, ignoreCase = true) 
                }) {
                    child.performAction(android.view.accessibility.AccessibilityNodeInfo.ACTION_CLICK)
                    isProcessing = false
                    Log.d(TAG, "✅ تم إغلاق الإعلان بنجاح")
                    return
                }
                
                findAndClickCloseButton(child, closeTexts)
            }
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في البحث عن زر الإغلاق", e)
        }
    }
    
    private fun clickTopRightCorner() {
        try {
            val root = rootInActiveWindow ?: return
            val rect = Rect()
            root.getBoundsInScreen(rect)
            
            val x = (rect.right - 50).toFloat()
            val y = (rect.top + 50).toFloat()
            
            performGesture(createClickGesture(x, y))
            isProcessing = false
            Log.d(TAG, "👆 تم النقر في الزاوية العلوية اليمنى")
        } catch (e: Exception) {
            Log.e(TAG, "خطأ في النقر على الزاوية", e)
            isProcessing = false
        }
    }
    
    private fun createClickGesture(x: Float, y: Float): GestureDescription {
        val path = Path()
        path.moveTo(x, y)
        
        val builder = GestureDescription.Builder()
        builder.addStroke(GestureDescription.StrokeDescription(path, 0, 1))
        return builder.build()
    }
    
    override fun onInterrupt() {
        Log.d(TAG, "⚠️ خدمة الإمكانية توقفت")
        isProcessing = false
    }
    
    override fun onDestroy() {
        super.onDestroy()
        instance = null
        Log.d(TAG, "❌ خدمة الإمكانية دمرت")
    }
}
