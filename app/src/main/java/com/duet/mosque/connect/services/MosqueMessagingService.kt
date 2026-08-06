package com.duet.mosque.connect.services

import android.content.Context
import android.util.Log
import com.duet.mosque.connect.utils.NotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MosqueMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("MosqueMessagingService", "New FCM token generated: $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("MosqueMessagingService", "FCM message received from: ${remoteMessage.from}")

        // Deduplication and Sender Check
        val secPrefs = getSharedPreferences("duet_mosque_sec_prefs", Context.MODE_PRIVATE)
        val myDeviceId = secPrefs.getString("my_device_unique_id", "")
        val senderId = remoteMessage.data["senderId"] ?: ""
        
        // Skip if this message is from us
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
            
            // Update last seen timestamp to prevent duplicate when app opens
            val lastSeen = secPrefs.getLong("last_notified_timestamp", 0L)
            if (ts > lastSeen) {
                secPrefs.edit().putLong("last_notified_timestamp", ts).apply()
            }
        }
    }
}
