package com.rinconsur.probadores.repository.data

import androidx.room.TypeConverter
import com.rinconsur.probadores.repository.models.EstadoProbador

class Converters {
    @TypeConverter
    fun estadoAString(estado: EstadoProbador): String = estado.name

    @TypeConverter
    fun stringAEstado(valor: String): EstadoProbador =
        runCatching { EstadoProbador.valueOf(valor) }.getOrDefault(EstadoProbador.LIBRE)
}
