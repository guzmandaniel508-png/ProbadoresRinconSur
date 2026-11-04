package com.rinconsur.probadores.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rinconsur.probadores.repository.data.AppDatabase
import com.rinconsur.probadores.repository.models.Rol
import com.rinconsur.probadores.repository.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PersonalFormUi(
    val id: Int = 0,
    val nombre: String = "",
    val email: String = "",
    val password: String = "",
    val rolId: Int? = null,
    val fotoUri: String? = null,
    val nombreError: String? = null,
    val emailError: String? = null,
    val passwordError: String? = null,
    val rolError: String? = null,
    val terminado: Boolean = false
) {
    val esEdicion: Boolean get() = id > 0
}

/** Formulario de alta / edición del personal con foto de perfil (CameraX). */
class PersonalFormViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.userDao()

    val roles: StateFlow<List<Rol>> = db.rolDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _ui = MutableStateFlow(PersonalFormUi())
    val ui: StateFlow<PersonalFormUi> = _ui.asStateFlow()

    private var cargado = false

    fun cargar(id: Int) {
        if (cargado) return
        cargado = true
        if (id <= 0) return
        viewModelScope.launch {
            val u = withContext(Dispatchers.IO) { dao.getById(id) } ?: return@launch
            _ui.update {
                it.copy(
                    id = u.id, nombre = u.nombre, email = u.email, password = u.password,
                    rolId = u.rolId, fotoUri = u.fotoUri
                )
            }
        }
    }

    fun onNombreChange(v: String) = _ui.update { it.copy(nombre = v, nombreError = Validaciones.validarNombre(v)) }
    fun onEmailChange(v: String) = _ui.update { it.copy(email = v, emailError = Validaciones.validarEmail(v)) }
    fun onPasswordChange(v: String) = _ui.update { it.copy(password = v, passwordError = Validaciones.validarPassword(v)) }
    fun onRolChange(id: Int) = _ui.update {
        it.copy(rolId = id, rolError = Validaciones.validarSeleccion(id, "Seleccione un rol"))
    }
    fun onFotoChange(uri: String?) = _ui.update { it.copy(fotoUri = uri) }

    fun guardar() {
        val s = _ui.value
        val nombreError = Validaciones.validarNombre(s.nombre)
        val emailError = Validaciones.validarEmail(s.email)
        val passError = Validaciones.validarPassword(s.password)
        val rolError = Validaciones.validarSeleccion(s.rolId, "Seleccione un rol")
        if (listOf(nombreError, emailError, passError, rolError).any { it != null }) {
            _ui.update {
                it.copy(nombreError = nombreError, emailError = emailError, passwordError = passError, rolError = rolError)
            }
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    val user = User(
                        id = s.id, nombre = s.nombre, email = s.email.trim(), password = s.password,
                        fotoUri = s.fotoUri, rolId = s.rolId!!
                    )
                    if (s.esEdicion) dao.update(user) else dao.insert(user)
                }
                _ui.update { it.copy(terminado = true) }
            } catch (e: SQLiteConstraintException) {
                _ui.update { it.copy(emailError = "Ya existe un usuario con el correo ${s.email.trim()}") }
            }
        }
    }
}
