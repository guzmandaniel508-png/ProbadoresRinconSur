package com.rinconsur.probadores.viewmodels

import com.rinconsur.probadores.repository.models.EstadoProbador
import com.rinconsur.probadores.repository.models.Probador
import kotlin.math.ceil

/**
 * Reglas de negocio del caso, sin dependencias de UI ni de Android.
 * La interfaz solo muestra los resultados que calculan estas funciones.
 */
object ReglasProbadores {

    const val PROMEDIO_DEFECTO_MIN = 10

    /** Segundos que lleva ocupado un probador (0 si no está ocupado). */
    fun segundosEnUso(p: Probador, ahora: Long): Long =
        p.ocupadoDesde?.let { ((ahora - it) / 1000).coerceAtLeast(0) } ?: 0L

    /** Un probador está "en alerta" si está ocupado y alcanzó/superó el tiempo máximo. */
    fun enAlerta(p: Probador, ahora: Long, limiteMin: Int): Boolean =
        p.estado == EstadoProbador.OCUPADO && segundosEnUso(p, ahora) >= limiteMin * 60L

    /** Avance (0..1) del tiempo de uso respecto del límite. */
    fun progreso(p: Probador, ahora: Long, limiteMin: Int): Float {
        if (limiteMin <= 0) return 0f
        return (segundosEnUso(p, ahora) / (limiteMin * 60f)).coerceIn(0f, 1f)
    }

    /**
     * Espera estimada para los clientes en fila:
     * si hay probadores libres o nadie espera, no hay espera; si no, se reparte la fila entre los
     * probadores ocupados y se multiplica por el tiempo promedio de uso.
     */
    fun esperaEstimadaMin(esperando: Int, libres: Int, ocupados: Int, promedioMin: Int): Int {
        if (esperando <= 0 || libres > 0 || ocupados <= 0) return 0
        val rondas = ceil(esperando.toDouble() / ocupados).toInt()
        return rondas * promedioMin
    }
}
