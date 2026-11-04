package com.rinconsur.probadores.repository.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Probador físico de la sección Vestuario.
 * ocupadoDesde: instante (ms epoch) en que se ocupó; null si no está ocupado.
 * alertaEnviada: true si ya se notificó el uso prolongado de la ocupación actual.
 */
@Entity(
    tableName = "probadores",
    indices = [Index(value = ["codigo"], unique = true)]
)
data class Probador(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val codigo: String,
    val zona: String,
    val estado: EstadoProbador = EstadoProbador.LIBRE,
    val ocupadoDesde: Long? = null,
    val prendas: Int = 0,
    val alertaEnviada: Boolean = false
) {
    companion object {
        val ZONAS = listOf("Mujer", "Hombre", "Infantil")
    }
}
