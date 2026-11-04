package com.rinconsur.probadores.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rinconsur.probadores.repository.data.AppDatabase
import com.rinconsur.probadores.repository.models.Rol
import com.rinconsur.probadores.repository.models.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val loggedUser: User? = null,
    val rolNombre: String = ""
) {
    /** El Jefe de sección administra probadores, personal y configuración. */
    val esAdmin: Boolean get() = loggedUser?.rolId == Rol.ID_JEFE
}

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Validación en vivo: cada cambio recalcula el error del campo
    fun onEmailChange(value: String) = _uiState.update {
        it.copy(email = value, emailError = Validaciones.validarEmail(value), error = null)
    }

    fun onPasswordChange(value: String) = _uiState.update {
        it.copy(password = value, passwordError = Validaciones.validarPasswordLogin(value), error = null)
    }

    fun login() {
        val s = _uiState.value
        val emailError = Validaciones.validarEmail(s.email)
        val passError = Validaciones.validarPasswordLogin(s.password)
        if (emailError != null || passError != null) {
            _uiState.update { it.copy(emailError = emailError, passwordError = passError) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            delay(500) // pausa breve para mostrar el indicador de carga
            val user = withContext(Dispatchers.IO) { db.userDao().login(s.email.trim(), s.password) }
            val rol = user?.let { withContext(Dispatchers.IO) { db.rolDao().getById(it.rolId)?.nombre } }
            _uiState.update {
                if (user != null) it.copy(isLoading = false, loggedUser = user, rolNombre = rol ?: "", password = "")
                else it.copy(isLoading = false, error = "Correo o contraseña incorrectos")
            }
        }
    }

    fun logout() {
        _uiState.value = LoginUiState()
    }
}
