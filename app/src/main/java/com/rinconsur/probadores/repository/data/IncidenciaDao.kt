package com.rinconsur.probadores.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.rinconsur.probadores.repository.models.Incidencia
import com.rinconsur.probadores.repository.models.IncidenciaConProbador
import kotlinx.coroutines.flow.Flow

@Dao
interface IncidenciaDao {
    @Query(
        "SELECT i.*, p.codigo AS codigo FROM incidencias i " +
            "INNER JOIN probadores p ON p.id = i.probadorId ORDER BY i.resuelta ASC, i.fecha DESC"
    )
    fun getAll(): Flow<List<IncidenciaConProbador>>

    @Query("SELECT * FROM incidencias WHERE id = :id")
    suspend fun getById(id: Int): Incidencia?

    @Query("SELECT COUNT(*) FROM incidencias WHERE resuelta = 0")
    fun contarPendientes(): Flow<Int>

    @Insert
    suspend fun insert(incidencia: Incidencia): Long

    @Update
    suspend fun update(incidencia: Incidencia)

    @Delete
    suspend fun delete(incidencia: Incidencia)

    @Query("UPDATE incidencias SET resuelta = :resuelta WHERE id = :id")
    suspend fun marcarResuelta(id: Int, resuelta: Boolean)
}
