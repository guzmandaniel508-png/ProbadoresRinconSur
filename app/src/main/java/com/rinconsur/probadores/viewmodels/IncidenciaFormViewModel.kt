package com.rinconsur.probadores.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rinconsur.probadores.repository.data.AppDatabase
import com.rinconsur.probadores.repository.models.Incidencia
import com.rinconsur.probadores.repository.models.Probador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class IncidenciaFormUi(
    val id: Int = 0,
    val probadorId: Int? = null,
    val descripcion: String = "",
    val fotoUri: String? = null,
    val probadorError: String? = null,
    val descripcionError: String? = null,
    val terminado: Boolean = false
) {
    val esEdicion: Boolean get() = id > 0
}

/** Formulario para reportar una incidencia (ej: prenda abandonada) con foto de evidencia. */
class IncidenciaFormViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val dao = db.incidenciaDao()

    val probadores: StateFlow<List<Probador>> = db.probadorDao().getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _ui = MutableStateFlow(IncidenciaFormUi())
    val ui: StateFlow<IncidenciaFormUi> = _ui.asStateFlow()

    private var original: Incidencia? = null
    private var iniciado = false

    /** id > 0 edita; probadorId > 0 deja preseleccionado el probador (viene desde su tarjeta). */
    fun iniciar(id: Int, probadorIdInicial: Int) {
        if (iniciado) return
        iniciado = true
        if (id > 0) {
            viewModelScope.launch {
                val i = withContext(Dispatchers.IO) { dao.getById(id) } ?: return@launch
                original = i
                _ui.update {
                    it.copy(id = i.id, probadorId = i.probadorId, descripcion = i.descripcion, fotoUri = i.fotoUri)
                }
            }
        } else if (probadorIdInicial > 0) {
            _ui.update { it.copy(probadorId = probadorIdInicial) }
        }
    }

    fun onProbadorChange(id: Int) = _ui.update {
        it.copy(probadorId = id, probadorError = Validaciones.validarSeleccion(id, "Seleccione un probador"))
    }

    fun onDescripcionChange(valor: String) = _ui.update {
        val v = valor.take(200)
        it.copy(descripcion = v, descripcionError = Validaciones.validarDescripcion(v))
    }

    fun onFotoChange(uri: String?) = _ui.update { it.copy(fotoUri = uri) }

    fun guardar(reportadoPor: String) {
        val s = _ui.value
        val probError = Validaciones.validarSeleccion(s.probadorId, "Seleccione un probador")
        val descError = Validaciones.validarDescripcion(s.descripcion)
        if (probError != null || descError != null) {
            _ui.update { it.copy(probadorError = probError, descripcionError = descError) }
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            val base = original
            if (base != null) {
                dao.update(
                    base.copy(probadorId = s.probadorId!!, descripcion = s.descripcion.trim(), fotoUri = s.fotoUri)
                )
            } else {
                dao.insert(
                    Incidencia(
                        probadorId = s.probadorId!!,
                        descripcion = s.descripcion.trim(),
                        fotoUri = s.fotoUri,
                        fecha = System.currentTimeMillis(),
                        reportadoPor = reportadoPor
                    )
                )
            }
            _ui.update { it.copy(terminado = true) }
        }
    }
}
