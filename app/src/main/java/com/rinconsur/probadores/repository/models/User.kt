package com.rinconsur.probadores.repository.models

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Personal de tienda que usa la aplicación. */
@Entity(
    tableName = "users",
    foreignKeys = [
        ForeignKey(
            entity = Rol::class,
            parentColumns = ["id"],
            childColumns = ["rolId"],
            onDelete = ForeignKey.RESTRICT,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["rolId"])
    ]
)
data class User(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val email: String,
    val password: String,            // Solo para fines académicos (sin hash)
    val fotoUri: String? = null,     // Foto de perfil (CameraX)
    val rolId: Int                   // FK -> roles.id
)
