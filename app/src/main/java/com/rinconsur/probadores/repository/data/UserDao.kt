package com.rinconsur.probadores.repository.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.rinconsur.probadores.repository.models.User
import com.rinconsur.probadores.repository.models.UsuarioConRol
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query(
        "SELECT u.*, r.nombre AS rolNombre FROM users u " +
            "INNER JOIN roles r ON r.id = u.rolId ORDER BY u.nombre ASC"
    )
    fun getAll(): Flow<List<UsuarioConRol>>

    @Query("SELECT * FROM users WHERE id = :id")
    suspend fun getById(id: Int): User?

    @Query("SELECT * FROM users WHERE email = :email AND password = :password LIMIT 1")
    suspend fun login(email: String, password: String): User?

    @Insert(onConflict = OnConflictStrategy.ABORT) // el email es único
    suspend fun insert(user: User): Long

    @Update
    suspend fun update(user: User)

    @Delete
    suspend fun delete(user: User)
}
