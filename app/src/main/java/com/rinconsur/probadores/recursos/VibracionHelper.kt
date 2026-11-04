package com.rinconsur.probadores.recursos

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

/**
 * RECURSO NATIVO 3: vibración del dispositivo (permiso normal VIBRATE).
 * Refuerza la alerta cuando el personal no está mirando la pantalla.
 */
object VibracionHelper {

    fun vibrarAlerta(context: Context) {
        val vibrador: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            context.getSystemService(VibratorManager::class.java)?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
        if (vibrador?.hasVibrator() == true) {
            // Patrón: pausa, vibra, pausa, vibra (sin repetición)
            vibrador.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 300), -1))
        }
    }
}
