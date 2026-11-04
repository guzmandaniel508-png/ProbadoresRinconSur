package com.rinconsur.probadores.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Incidencia en un probador (ej: prenda abandonada). Puede incluir foto de evidencia. */
@Entity(
    tableName = "incidencias",
    foreignKeys = [
        ForeignKey(
            entity = Probador::class,
            parentColumns = ["id"],
            childColumns = ["probadorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["probadorId"])]
)
data class Incidencia(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val probadorId: Int,
    val descripcion: String,
    val fotoUri: String? = null,
    val fecha: Long,
    val resuelta: Boolean = false,
    val reportadoPor: String
)
