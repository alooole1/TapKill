package com.tapkill

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.Toast

class TapKillAccessibilityService : AccessibilityService() {

    companion object {
        private const val TAG = "TapKillAccessibility"
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            if (it.eventType == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
                // حاول العثور على زر الإغلاق في النافذة الجديدة
                findAndClickCloseButton()
            }
        }
    }

    override fun onInterrupt() {
        Log.d(TAG, "Service interrupted")
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        // تكوين إضافي إن لزم
        Log.d(TAG, "Accessibility Service connected")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.getStringExtra("ACTION") == "KILL_AD") {
            findAndClickCloseButton()
        }
        return START_STICKY
    }

    private fun findAndClickCloseButton() {
        val root = rootInActiveWindow ?: return

        // البحث عن عناصر تحتوي على نص "إغلاق" أو "Close" أو "X" أو "Skip"
        val closeKeywords = listOf("إغلاق", "Close", "X", "Skip", "تخطي", "تجاوز")
        val nodes = root.findAccessibilityNodeInfosByText(".*(" + closeKeywords.joinToString("|") + ").*".toRegex())

        // إذا وجدنا عناصر، نضغط على أول عنصر قابل للنقر
        for (node in nodes) {
            if (node.isClickable) {
                node.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                Toast.makeText(this, "تم النقر على زر الإغلاق!", Toast.LENGTH_SHORT).show()
                return
            }
            // إذا كان العنصر غير قابل للنقر، نحاول النقر على أبيه
            var parent = node.parent
            while (parent != null) {
                if (parent.isClickable) {
                    parent.performAction(AccessibilityNodeInfo.ACTION_CLICK)
                    Toast.makeText(this, "تم النقر على الزر (من خلال الأب)", Toast.LENGTH_SHORT).show()
                    return
                }
                parent = parent.parent
            }
        }

        // بحث أكثر عمومية عن أي زر يحوي معرف أو وصف مثل "close", "dismiss", "cancel"
        findAndClickByViewId()
    }

    private fun findAndClickByViewId() {
        val root = rootInActiveWindow ?: return
        // هنا يمكنك إضافة معرفات محددة لأزرار الإغلاق في التطبيقات المشهورة
        // مثل com.google.android.gms:id/cancel, com.facebook.ads:id/close, etc.
        // لكن للتبسيط، سنتركها عامة
    }
}
