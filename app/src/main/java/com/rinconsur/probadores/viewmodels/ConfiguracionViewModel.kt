package com.rinconsur.probadores.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rinconsur.probadores.repository.data.AppDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ConfiguracionUi(
    val limite: String = "",
    val limiteError: String? = null,
    val guardado: Boolean = false
)

/** Define el tiempo máximo de uso que dispara la alerta de probador prolongado. */
class ConfiguracionViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).configuracionDao()

    private val _ui = MutableStateFlow(ConfiguracionUi())
    val ui: StateFlow<ConfiguracionUi> = _ui.asStateFlow()

    init {
        viewModelScope.launch {
            val cfg = withContext(Dispatchers.IO) { dao.obtener() }
            _ui.update { it.copy(limite = (cfg?.limiteMinutos ?: 15).toString()) }
        }
    }

    fun onLimiteChange(valor: String) = _ui.update {
        val v = valor.filter { c -> c.isDigit() }.take(3)
        it.copy(limite = v, limiteError = Validaciones.validarLimiteMinutos(v), guardado = false)
    }

    fun guardar() {
        val s = _ui.value
        val error = Validaciones.validarLimiteMinutos(s.limite)
        if (error != null) {
            _ui.update { it.copy(limiteError = error) }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            dao.setLimite(s.limite.trim().toInt())
            _ui.update { it.copy(guardado = true) }
        }
    }
}
