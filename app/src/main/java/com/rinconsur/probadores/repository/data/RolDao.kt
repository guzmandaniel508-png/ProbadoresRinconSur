package com.rinconsur.probadores.repository.data

import androidx.room.Dao
import androidx.room.Query
import com.rinconsur.probadores.repository.models.Rol
import kotlinx.coroutines.flow.Flow

@Dao
interface RolDao {
    @Query("SELECT * FROM roles ORDER BY id ASC")
    fun getAll(): Flow<List<Rol>>

    @Query("SELECT * FROM roles WHERE id = :id")
    suspend fun getById(id: Int): Rol?
}
