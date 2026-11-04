package com.rinconsur.probadores.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.rinconsur.probadores.repository.data.AppDatabase
import com.rinconsur.probadores.repository.models.Incidencia
import com.rinconsur.probadores.repository.models.IncidenciaConProbador
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Lista de incidencias: leer, resolver y eliminar. */
class IncidenciaViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getDatabase(application).incidenciaDao()

    val incidencias: StateFlow<List<IncidenciaConProbador>> = dao.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun alternarResuelta(i: Incidencia) {
        viewModelScope.launch(Dispatchers.IO) { dao.marcarResuelta(i.id, !i.resuelta) }
    }

    fun eliminar(i: Incidencia) {
        viewModelScope.launch(Dispatchers.IO) { dao.delete(i) }
    }
}
