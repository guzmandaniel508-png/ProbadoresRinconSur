package com.rinconsur.probadores.repository.data

import androidx.room.Dao
import androidx.room.Query
import com.rinconsur.probadores.repository.models.Configuracion
import kotlinx.coroutines.flow.Flow

@Dao
interface ConfiguracionDao {
    @Query("SELECT * FROM configuracion WHERE id = 1")
    fun observar(): Flow<Configuracion?>

    @Query("SELECT * FROM configuracion WHERE id = 1")
    suspend fun obtener(): Configuracion?

    @Query("UPDATE configuracion SET limiteMinutos = :minutos WHERE id = 1")
    suspend fun setLimite(minutos: Int)

    @Query("UPDATE configuracion SET clientesEnEspera = :cantidad WHERE id = 1")
    suspend fun setEspera(cantidad: Int)
}
