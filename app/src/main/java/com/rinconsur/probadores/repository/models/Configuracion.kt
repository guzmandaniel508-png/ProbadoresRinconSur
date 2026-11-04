package com.rinconsur.probadores.repository.models

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Fila única (id = 1): tiempo máximo de uso y cantidad de clientes esperando. */
@Entity(tableName = "configuracion")
data class Configuracion(
    @PrimaryKey val id: Int = 1,
    val limiteMinutos: Int = 15,
    val clientesEnEspera: Int = 0
)
