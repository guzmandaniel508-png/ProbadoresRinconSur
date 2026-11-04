package com.rinconsur.probadores.recursos

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.rinconsur.probadores.MainActivity
import com.rinconsur.probadores.R

/**
 * RECURSO NATIVO 2: notificaciones.
 * Avisa al personal cuando un probador supera el tiempo máximo de uso.
 * En Android 13+ requiere el permiso POST_NOTIFICATIONS en tiempo de ejecución.
 */
object NotificacionHelper {

    const val CANAL_ALERTAS = "alertas_probadores"

    /** Crea el canal de notificaciones (obligatorio desde Android 8, minSdk = 26). */
    fun crearCanal(context: Context) {
        val canal = NotificationChannel(
            CANAL_ALERTAS,
            "Alertas de probadores",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Avisos de probadores con uso prolongado"
            enableVibration(true)
        }
        context.getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
    }

    fun tienePermiso(context: Context): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // el permiso se verifica con tienePermiso()
    fun alertaUsoProlongado(context: Context, probadorId: Int, codigo: String, minutos: Int) {
        if (!tienePermiso(context)) return

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context, probadorId, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val notificacion = NotificationCompat.Builder(context, CANAL_ALERTAS)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("Probador $codigo con uso prolongado")
            .setContentText("Lleva $minutos min ocupado. Revisa si hay prendas abandonadas.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(1000 + probadorId, notificacion)
    }
}
