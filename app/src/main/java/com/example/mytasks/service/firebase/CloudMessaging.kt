package com.example.mytasks.service.firebase

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.mytasks.MainActivity
import com.example.mytasks.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class CloudMessaging : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        if (remoteMessage.notification != null) {
            Log.d("remoteMessage", "Message Notification Body: " + remoteMessage.notification!!.body);

            val title = remoteMessage.notification?.title
            val body = remoteMessage.notification?.body

            sendNotification(title, body)
        }



    }

    private fun sendNotification(title: String?, body: String?) {
        val intent = Intent(baseContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(baseContext, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val channel = getString(R.string.default_notification_channel_id)
        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val logoImage = BitmapFactory.decodeResource(baseContext.resources, R.drawable.logo_task)


        val notificationManager: NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notification = NotificationCompat.Builder(baseContext, channel)
            .setSmallIcon(R.drawable.ic_baseline_check_circle_outline_24)
            .setLargeIcon(logoImage)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setSound(sound)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelVersion = NotificationChannel(channel, "channel", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channelVersion)
        }

        notificationManager.notify(0, notification.build())
    }
}