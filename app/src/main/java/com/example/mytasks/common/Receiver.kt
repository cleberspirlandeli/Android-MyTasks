package com.example.mytasks.common

import android.app.AlarmManager
import android.app.NotificationChannelGroup
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.provider.Settings.Global.getString
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.mytasks.MainActivity
import com.example.mytasks.R

class Receiver: BroadcastReceiver() {

    private lateinit var notification: NotificationCompat.Builder

    override fun onReceive(context: Context, intent: Intent) {

        val bundle = intent.extras

        if (bundle == null )
            return

        val title = bundle.getString("task")
        val notificationId = bundle.getInt("notificationId")

        val message = context.getString(R.string.message_notification)

        val intentNotification = Intent(context, MainActivity::class.java)
        intentNotification.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(context, notificationId, intentNotification, PendingIntent.FLAG_ONE_SHOT)

        val sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val logoImage = BitmapFactory.decodeResource(context.resources, R.drawable.logo_task)

        notification = NotificationCompat.Builder(context, "notifyMyTask")
            .setSmallIcon(R.drawable.ic_baseline_check_circle_outline_24)
            .setLargeIcon(logoImage)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setSound(sound)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        var notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(200, notification.build())
    }
}