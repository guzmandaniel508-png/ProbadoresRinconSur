package com.rinconsur.probadores.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rinconsur.probadores.repository.data.AppDatabase
import com.rinconsur.probadores.repository.models.User
import com.rinconsur.probadores.repository.models.UsuarioConRol
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lista del personal de tienda (solo el Jefe de sección accede). */
class PersonalViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).userDao()

    val personal: StateFlow<List<UsuarioConRol>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()
    fun limpiarMensaje() { _mensaje.value = null }

    fun eliminar(user: User, usuarioActualId: Int?) {
        if (user.id == usuarioActualId) {
            _mensaje.value = "No puedes eliminar tu propio usuario"
            return
        }
        viewModelScope.launch(Dispatchers.IO) { dao.delete(user) }
    }
}
