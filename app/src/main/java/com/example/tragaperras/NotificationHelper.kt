package com.example.tragaperras

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

// Helper para gestionar la creación del canal y envío de notificaciones de victoria
class NotificationHelper(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "victory_channel"
        const val CHANNEL_NAME = "Notificaciones de victoria"
        const val NOTIFICATION_ID = 1001
        const val EXTRA_RESOLUTION_TIME = "extra_resolution_time"
    }

    init {
        createNotificationChannel()
    }

    // Crea el canal de notificaciones necesario a partir de Android 8.0 (API 26)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Canal para notificar victorias en el juego"
            }
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Lanza la notificación con el icono de la app y un PendingIntent con el tiempo de resolución
    fun sendVictoryNotification(durationSeconds: Double) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
        }

        val intent = Intent(context, GameActivity::class.java).apply {
            putExtra(EXTRA_RESOLUTION_TIME, durationSeconds)
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formattedTime = String.format(java.util.Locale.getDefault(), "%.2f", durationSeconds)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("¡Victoria!")
            .setContentText("¡Has ganado! Tiempo de resolución: $formattedTime s")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        with(NotificationManagerCompat.from(context)) {
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}
