package com.rinconsur.probadores.viewmodels

import android.app.Application
import android.database.sqlite.SQLiteConstraintException
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rinconsur.probadores.repository.data.AppDatabase
import com.rinconsur.probadores.repository.models.EstadoProbador
import com.rinconsur.probadores.repository.models.Probador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProbadorFormUi(
    val id: Int = 0,
    val codigo: String = "",
    val zona: String = "",
    val codigoError: String? = null,
    val zonaError: String? = null,
    val terminado: Boolean = false,
    val mensaje: String? = null
) {
    val esEdicion: Boolean get() = id > 0
}

/** Formulario de alta / edición de probadores. */
class ProbadorFormViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).probadorDao()

    private val _ui = MutableStateFlow(ProbadorFormUi())
    val ui: StateFlow<ProbadorFormUi> = _ui.asStateFlow()

    private var original: Probador? = null
    private var cargado = false

    fun cargar(id: Int) {
        if (cargado) return
        cargado = true
        if (id <= 0) return
        viewModelScope.launch {
            val p = withContext(Dispatchers.IO) { dao.getById(id) } ?: return@launch
            original = p
            _ui.update { it.copy(id = p.id, codigo = p.codigo, zona = p.zona) }
        }
    }

    fun onCodigoChange(valor: String) = _ui.update {
        val v = valor.uppercase().take(6)
        it.copy(codigo = v, codigoError = Validaciones.validarCodigoProbador(v), mensaje = null)
    }

    fun onZonaChange(valor: String) = _ui.update {
        it.copy(zona = valor, zonaError = Validaciones.validarZona(valor))
    }

    fun guardar() {
        val s = _ui.value
        val codigoError = Validaciones.validarCodigoProbador(s.codigo)
        val zonaError = Validaciones.validarZona(s.zona)
        if (codigoError != null || zonaError != null) {
            _ui.update { it.copy(codigoError = codigoError, zonaError = zonaError) }
            return
        }
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    if (s.esEdicion) {
                        val base = original ?: dao.getById(s.id)!!
                        dao.update(base.copy(codigo = s.codigo, zona = s.zona))
                    } else {
                        dao.insert(Probador(codigo = s.codigo, zona = s.zona))
                    }
                }
                _ui.update { it.copy(terminado = true) }
            } catch (e: SQLiteConstraintException) {
                _ui.update { it.copy(codigoError = "Ya existe un probador con el código ${s.codigo}") }
            }
        }
    }

    fun eliminar() {
        val p = original ?: return
        if (p.estado == EstadoProbador.OCUPADO) {
            _ui.update { it.copy(mensaje = "No se puede eliminar un probador ocupado") }
            return
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) { dao.delete(p) }
            _ui.update { it.copy(terminado = true) }
        }
    }
}
