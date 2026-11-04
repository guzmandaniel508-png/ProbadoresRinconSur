package com.rinconsur.probadores.repository.models

import androidx.room.Embedded

/** Resultados de consultas con JOIN (se usan solo para lectura). */
data class RegistroConProbador(
    @Embedded val registro: RegistroUso,
    val codigo: String,
    val zona: String
)

data class IncidenciaConProbador(
    @Embedded val incidencia: Incidencia,
    val codigo: String
)

data class UsuarioConRol(
    @Embedded val usuario: User,
    val rolNombre: String
)
