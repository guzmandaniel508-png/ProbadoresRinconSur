package com.rinconsur.probadores.repository.models

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** Rol del personal de tienda (Jefe de sección, Supervisor, Vendedor). */
@Entity(
    tableName = "roles",
    indices = [Index(value = ["nombre"], unique = true)]
)
data class Rol(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String
) {
    companion object {
        const val ID_JEFE = 1 // el Jefe de sección puede administrar probadores, personal y configuración
    }
}
