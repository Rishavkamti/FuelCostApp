package com.rishav.fuelcost.service

import android.accessibilityservice.AccessibilityService
import android.graphics.PixelFormat
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import com.rishav.fuelcost.R
import com.rishav.fuelcost.util.FuelCalculator
import com.rishav.fuelcost.util.MapsHelper
import com.rishav.fuelcost.util.Prefs

/**
 * Ye service Zomato/Swiggy delivery-partner app ki screen ka text padhti hai.
 *
 * IMPORTANT: extractPickupAndDrop() ke andar wali logic ek STARTING POINT hai.
 * Real app khol ke exact text jo screen pe dikhta hai (jaise "Pickup:", "Drop:",
 * ya address ka format) dekh kar isko tune karna padega — har app ka UI text
 * alag hota hai aur time ke saath badalta rehta hai.
 */
class OrderAccessibilityService : AccessibilityService() {

    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private val handler = Handler(Looper.getMainLooper())
    private var lastProcessedKey: String? = null

    override fun onServiceConnected() {
        super.onServiceConnected()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) return

        val root = rootInActiveWindow ?: return
        val allText = StringBuilder()
        collectText(root, allText)
        root.recycle()

        val screenText = allText.toString()
        val (pickup, drop) = extractPickupAndDrop(screenText) ?: return

        val key = "$pickup|$drop"
        if (key == lastProcessedKey) return // isi order ko baar baar process na kare
        lastProcessedKey = key

        val apiKey = Prefs.getMapsApiKey(this)
        if (apiKey.isEmpty()) return

        MapsHelper.getDistance(pickup, drop, apiKey, object : MapsHelper.DistanceCallback {
            override fun onResult(distanceKm: Float, durationText: String) {
                val mileage = Prefs.getMileage(this@OrderAccessibilityService)
                val fuelPrice = Prefs.getFuelPrice(this@OrderAccessibilityService)
                val cost = FuelCalculator.calculateCost(distanceKm, mileage, fuelPrice)
                handler.post {
                    showOverlay(
                        String.format(
                            "Distance: %.1f km (%s)\nFuel cost: ₹%.1f",
                            distanceKm, durationText, cost
                        )
                    )
                }
            }

            override fun onError(message: String) {
                // Chup chaap fail ho jaane do - agli baar retry karega jab naya order aayega
            }
        })
    }

    private fun collectText(node: AccessibilityNodeInfo?, out: StringBuilder) {
        if (node == null) return
        node.text?.let { out.append(it).append(" | ") }
        for (i in 0 until node.childCount) {
            collectText(node.getChild(i), out)
        }
    }

    /**
     * PLACEHOLDER LOGIC — isko real app screen dekh kar adjust karo.
     * Filhaal ye "Pickup" aur "Drop" (ya "Delivery") keywords ke baad wala
     * text dhundhne ki koshish karta hai.
     */
    private fun extractPickupAndDrop(text: String): Pair<String, String>? {
        val pickupRegex = Regex("(Pickup|Restaurant)[:\\s]+([^|]{5,80})", RegexOption.IGNORE_CASE)
        val dropRegex = Regex("(Drop|Delivery|Customer)[:\\s]+([^|]{5,80})", RegexOption.IGNORE_CASE)

        val pickupMatch = pickupRegex.find(text) ?: return null
        val dropMatch = dropRegex.find(text) ?: return null

        val pickup = pickupMatch.groupValues[2].trim()
        val drop = dropMatch.groupValues[2].trim()
        if (pickup.isEmpty() || drop.isEmpty()) return null
        return Pair(pickup, drop)
    }

    private fun showOverlay(message: String) {
        removeOverlay()
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.overlay_result, null)
        view.findViewById<TextView>(R.id.overlayText).text = message

        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        )
        params.gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
        params.y = 100

        windowManager?.addView(view, params)
        overlayView = view

        // 12 second baad overlay hata do
        handler.postDelayed({ removeOverlay() }, 12000)
    }

    private fun removeOverlay() {
        overlayView?.let {
            try {
                windowManager?.removeView(it)
            } catch (_: Exception) {
            }
        }
        overlayView = null
    }

    override fun onInterrupt() {}
}
