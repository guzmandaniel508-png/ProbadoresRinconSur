package com.rinconsur.probadores.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Historial: una fila por cada vez que un probador fue ocupado y luego liberado. */
@Entity(
    tableName = "registros_uso",
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
data class RegistroUso(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val probadorId: Int,
    val inicio: Long,
    val fin: Long,
    val prendas: Int,
    val duracionSeg: Long
)
