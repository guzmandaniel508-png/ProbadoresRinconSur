package com.rinconsur.probadores.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rinconsur.probadores.repository.models.Probador
import kotlinx.coroutines.flow.Flow

@Dao
interface ProbadorDao {
    @Query("SELECT * FROM probadores ORDER BY codigo ASC")
    fun getAll(): Flow<List<Probador>>

    @Query("SELECT * FROM probadores WHERE id = :id")
    suspend fun getById(id: Int): Probador?

    @Insert(onConflict = OnConflictStrategy.ABORT) // el código es único
    suspend fun insert(probador: Probador): Long

    @Update
    suspend fun update(probador: Probador)

    @Delete
    suspend fun delete(probador: Probador)

    @Query("UPDATE probadores SET estado = 'OCUPADO', ocupadoDesde = :desde, prendas = :prendas, alertaEnviada = 0 WHERE id = :id")
    suspend fun ocupar(id: Int, desde: Long, prendas: Int)

    @Query("UPDATE probadores SET estado = 'LIBRE', ocupadoDesde = NULL, prendas = 0, alertaEnviada = 0 WHERE id = :id")
    suspend fun liberar(id: Int)

    @Query("UPDATE probadores SET estado = 'MANTENCION', ocupadoDesde = NULL, prendas = 0, alertaEnviada = 0 WHERE id = :id")
    suspend fun enviarAMantencion(id: Int)

    @Query("UPDATE probadores SET alertaEnviada = 1 WHERE id = :id")
    suspend fun marcarAlerta(id: Int)
}
