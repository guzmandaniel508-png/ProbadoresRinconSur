package com.rinconsur.probadores.view.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Funciones de formato de fechas y duraciones para mostrar en pantalla. */
object Formato {
    private val local = Locale("es", "CL")

    fun fechaHora(ms: Long): String = SimpleDateFormat("dd/MM HH:mm", local).format(Date(ms))

    fun hora(ms: Long): String = SimpleDateFormat("HH:mm", local).format(Date(ms))

    /** 125 -> "02:05" */
    fun mmss(segundos: Long): String {
        val s = segundos.coerceAtLeast(0)
        return "%02d:%02d".format(s / 60, s % 60)
    }

    /** 125 -> "2 min 5 s" */
    fun duracion(segundos: Long): String {
        val s = segundos.coerceAtLeast(0)
        return "${s / 60} min ${s % 60} s"
    }
}
