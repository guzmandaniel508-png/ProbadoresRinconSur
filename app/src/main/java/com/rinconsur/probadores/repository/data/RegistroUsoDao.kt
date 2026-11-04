package com.rinconsur.probadores.repository.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.rinconsur.probadores.repository.models.RegistroConProbador
import com.rinconsur.probadores.repository.models.RegistroUso
import kotlinx.coroutines.flow.Flow

@Dao
interface RegistroUsoDao {
    @Insert
    suspend fun insert(registro: RegistroUso): Long

    @Query(
        "SELECT r.*, p.codigo AS codigo, p.zona AS zona FROM registros_uso r " +
            "INNER JOIN probadores p ON p.id = r.probadorId ORDER BY r.fin DESC"
    )
    fun getAll(): Flow<List<RegistroConProbador>>

    /** Duración promedio de uso en segundos (null si aún no hay historial). */
    @Query("SELECT AVG(duracionSeg) FROM registros_uso")
    fun promedioDuracionSeg(): Flow<Double?>
}
