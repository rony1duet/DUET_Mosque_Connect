package com.duet.mosque.connect.services

import android.content.Context
import android.util.Log
import com.duet.mosque.connect.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * =========================================================================================
 * FIREBASE CLOUD MESSAGING (FCM) BACKGROUND SERVICE (DUET Mosque Connect)
 * =========================================================================================
 * An Android background Service registered in `AndroidManifest.xml` that receives remote
 * push notifications even when the app is completely closed or in the background.
 *
 * Execution Flow:
 *  1. [Imam Action]: Mosque Imam publishes an announcement, Janaza notice, or updated Jamat time.
 *  2. [Firebase FCM Topic]: Firebase pushes a message to the "global_updates" topic.
 *  3. [Background Delivery]: The Android OS wakes up [MosqueMessagingService] and calls [onMessageReceived].
 *  4. [Self-Filter Check]: If `senderId == myDeviceId`, the message is ignored to prevent self-echoing.
 *  5. [System Alert]: [NotificationHelper.triggerSystemNotification] posts the alert to the device's status bar.
 *
 * Kotlin Concepts Explained for Beginners:
 *  - `class ... : FirebaseMessagingService()`: Subclasses the Android Service component provided by Firebase SDK.
 *  - `override fun onMessageReceived(...)`: Overrides the callback that fires when an incoming packet arrives.
 *  - Elvis operator `?:`: Kotlin null-coalescing operator (`val title = remoteMessage.title ?: "Default"`).
 * =========================================================================================
 */
class MosqueMessagingService : FirebaseMessagingService() {

    /**
     * Called when a new Firebase token is generated or refreshed for this device.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("MosqueMessagingService", "New FCM token generated: $token")
    }

    /**
     * Called when an incoming Firebase Cloud Message is received.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("MosqueMessagingService", "FCM message received from: ${remoteMessage.from}")

        val secPrefs = getSharedPreferences("duet_mosque_sec_prefs", Context.MODE_PRIVATE)
        val myDeviceId = secPrefs.getString("my_device_unique_id", "")
        val senderId = remoteMessage.data["senderId"] ?: ""

        // Prevent self-notification if this device sent the message
        if (senderId.isNotEmpty() && senderId == myDeviceId) {
            Log.d("MosqueMessagingService", "Skipping self-notification from FCM")
            return
        }

        val title = remoteMessage.notification?.title ?: remoteMessage.data["title"] ?: "New Mosque Update"
        val body = remoteMessage.notification?.body ?: remoteMessage.data["body"] ?: "Tap to view the latest updates from DUET Central Mosque."
        val tsStr = remoteMessage.data["timestamp"]
        val ts = tsStr?.toLongOrNull() ?: System.currentTimeMillis()

        val eventNoticesEnabled = secPrefs.getBoolean("pref_event_notices", true)

        if (eventNoticesEnabled) {
            NotificationHelper.triggerSystemNotification(
                context = applicationContext,
                title = title,
                body = body,
                soundEnabled = secPrefs.getBoolean("pref_adhan_sound", true),
                vibrateEnabled = true
            )

            // Update last seen timestamp to prevent duplicate popup when the app opens
            val lastSeen = secPrefs.getLong("last_notified_timestamp", 0L)
            if (ts > lastSeen) {
                secPrefs.edit().putLong("last_notified_timestamp", ts).apply()
            }
        }
    }
}
