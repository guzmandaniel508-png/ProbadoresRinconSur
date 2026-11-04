package com.rinconsur.probadores.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.withTransaction
import com.rinconsur.probadores.recursos.NotificacionHelper
import com.rinconsur.probadores.recursos.VibracionHelper
import com.rinconsur.probadores.repository.data.AppDatabase
import com.rinconsur.probadores.repository.models.Configuracion
import com.rinconsur.probadores.repository.models.EstadoProbador
import com.rinconsur.probadores.repository.models.Probador
import com.rinconsur.probadores.repository.models.RegistroUso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.roundToInt

/** Resumen que muestra el panel de inicio. */
data class ResumenUi(
    val total: Int = 0,
    val libres: Int = 0,
    val ocupados: Int = 0,
    val enAlerta: Int = 0,
    val mantencion: Int = 0,
    val clientesEnEspera: Int = 0,
    val esperaEstimadaMin: Int = 0,
    val limiteMinutos: Int = 15
)

/** Estado del diálogo "Ocupar probador" (null = cerrado). */
data class OcuparUi(
    val probadorId: Int,
    val codigo: String,
    val prendas: String = "",
    val error: String? = null
)

enum class FiltroProbadores(val etiqueta: String) {
    TODOS("Todos"),
    LIBRES("Libres"),
    OCUPADOS("Ocupados"),
    ALERTA("En alerta"),
    MANTENCION("Mantención")
}

/**
 * ViewModel principal: estado de los probadores, cronómetro, alertas y fila de espera.
 * Tiene alcance de Activity para que el monitoreo siga activo mientras la app está abierta.
 */
class ProbadorViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val probadorDao = db.probadorDao()
    private val registroDao = db.registroUsoDao()
    private val configDao = db.configuracionDao()
    private val appContext = application.applicationContext

    // ---- Reloj: emite la hora actual cada segundo (lo observan los cronómetros) ----
    private val _ahora = MutableStateFlow(System.currentTimeMillis())
    val ahora: StateFlow<Long> = _ahora.asStateFlow()

    // ---- Datos desde Room (Eagerly: el monitoreo de alertas necesita los datos siempre) ----
    val probadores: StateFlow<List<Probador>> = probadorDao.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val configuracion: StateFlow<Configuracion> = configDao.observar()
        .map { it ?: Configuracion() }
        .stateIn(viewModelScope, SharingStarted.Eagerly, Configuracion())

    private val promedioMin: Flow<Int> = registroDao.promedioDuracionSeg().map { seg ->
        if (seg == null) ReglasProbadores.PROMEDIO_DEFECTO_MIN else max(1, (seg / 60.0).roundToInt())
    }

    // ---- Resumen del panel (se recalcula con cada cambio de datos o de reloj) ----
    val resumen: StateFlow<ResumenUi> =
        combine(probadores, configuracion, ahora, promedioMin) { lista, cfg, t, prom ->
            val libres = lista.count { it.estado == EstadoProbador.LIBRE }
            val ocupados = lista.count { it.estado == EstadoProbador.OCUPADO }
            ResumenUi(
                total = lista.size,
                libres = libres,
                ocupados = ocupados,
                enAlerta = lista.count { ReglasProbadores.enAlerta(it, t, cfg.limiteMinutos) },
                mantencion = lista.count { it.estado == EstadoProbador.MANTENCION },
                clientesEnEspera = cfg.clientesEnEspera,
                esperaEstimadaMin = ReglasProbadores.esperaEstimadaMin(cfg.clientesEnEspera, libres, ocupados, prom),
                limiteMinutos = cfg.limiteMinutos
            )
        }.stateIn(viewModelScope, SharingStarted.Eagerly, ResumenUi())

    /** Probadores que superaron el tiempo máximo (se muestran en el panel). */
    val enAlerta: StateFlow<List<Probador>> =
        combine(probadores, configuracion, ahora) { lista, cfg, t ->
            lista.filter { ReglasProbadores.enAlerta(it, t, cfg.limiteMinutos) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---- Filtro de la lista ----
    private val _filtro = MutableStateFlow(FiltroProbadores.TODOS)
    val filtro: StateFlow<FiltroProbadores> = _filtro.asStateFlow()
    fun setFiltro(f: FiltroProbadores) { _filtro.value = f }

    val probadoresFiltrados: StateFlow<List<Probador>> =
        combine(probadores, filtro, configuracion, ahora) { lista, f, cfg, t ->
            when (f) {
                FiltroProbadores.TODOS -> lista
                FiltroProbadores.LIBRES -> lista.filter { it.estado == EstadoProbador.LIBRE }
                FiltroProbadores.OCUPADOS -> lista.filter { it.estado == EstadoProbador.OCUPADO }
                FiltroProbadores.ALERTA -> lista.filter { ReglasProbadores.enAlerta(it, t, cfg.limiteMinutos) }
                FiltroProbadores.MANTENCION -> lista.filter { it.estado == EstadoProbador.MANTENCION }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // ---- Diálogo "Ocupar" ----
    private val _ocupar = MutableStateFlow<OcuparUi?>(null)
    val ocupar: StateFlow<OcuparUi?> = _ocupar.asStateFlow()

    // ---- Mensajes para la UI (Snackbar) ----
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()
    fun limpiarMensaje() { _mensaje.value = null }

    // Ocupaciones ya notificadas (clave = id + instante de inicio) para no repetir avisos
    private val notificados = mutableSetOf<Pair<Int, Long>>()

    init {
        viewModelScope.launch {
            while (isActive) {
                val t = System.currentTimeMillis()
                _ahora.value = t
                revisarAlertas(t)
                delay(1000)
            }
        }
    }

    /** Detecta probadores con uso prolongado y avisa con notificación + vibración. */
    private suspend fun revisarAlertas(t: Long) {
        val limite = configuracion.value.limiteMinutos
        for (p in probadores.value) {
            if (!ReglasProbadores.enAlerta(p, t, limite) || p.alertaEnviada) continue
            val clave = p.id to (p.ocupadoDesde ?: 0L)
            if (clave in notificados) continue
            // Sin permiso de notificaciones se reintenta: el aviso sale apenas se conceda
            if (!NotificacionHelper.tienePermiso(appContext)) continue

            notificados.add(clave)
            withContext(Dispatchers.IO) { probadorDao.marcarAlerta(p.id) }
            val minutos = (ReglasProbadores.segundosEnUso(p, t) / 60).toInt()
            NotificacionHelper.alertaUsoProlongado(appContext, p.id, p.codigo, minutos)
            VibracionHelper.vibrarAlerta(appContext)
        }
    }

    // ---------------------------------------------------------------------------------------
    // Acciones sobre probadores
    // ---------------------------------------------------------------------------------------
    fun abrirOcupar(p: Probador) { _ocupar.value = OcuparUi(p.id, p.codigo) }
    fun cerrarOcupar() { _ocupar.value = null }

    fun onPrendasChange(valor: String) {
        _ocupar.update { it?.copy(prendas = valor, error = Validaciones.validarPrendas(valor)) }
    }

    fun confirmarOcupar() {
        val estado = _ocupar.value ?: return
        val error = Validaciones.validarPrendas(estado.prendas)
        if (error != null) {
            _ocupar.value = estado.copy(error = error)
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            probadorDao.ocupar(estado.probadorId, System.currentTimeMillis(), estado.prendas.trim().toInt())
        }
        _mensaje.value = "Probador ${estado.codigo} ocupado"
        _ocupar.value = null
    }

    /** Libera el probador y guarda el registro en el historial (en una sola transacción). */
    fun liberar(p: Probador) {
        val inicio = p.ocupadoDesde ?: System.currentTimeMillis()
        viewModelScope.launch {
            val fin = System.currentTimeMillis()
            withContext(Dispatchers.IO) {
                db.withTransaction {
                    registroDao.insert(
                        RegistroUso(
                            probadorId = p.id,
                            inicio = inicio,
                            fin = fin,
                            prendas = p.prendas,
                            duracionSeg = ((fin - inicio) / 1000).coerceAtLeast(0)
                        )
                    )
                    probadorDao.liberar(p.id)
                }
            }
            _mensaje.value = "Probador ${p.codigo} liberado"
        }
    }

    /** Envía a mantención un probador libre, o habilita uno que estaba en mantención. */
    fun alternarMantencion(p: Probador) {
        if (p.estado == EstadoProbador.OCUPADO) {
            _mensaje.value = "Libera el probador antes de enviarlo a mantención"
            return
        }
        viewModelScope.launch(Dispatchers.IO) {
            if (p.estado == EstadoProbador.LIBRE) {
                probadorDao.enviarAMantencion(p.id)
            } else {
                probadorDao.liberar(p.id) // MANTENCION -> LIBRE
            }
        }
    }

    // ---------------------------------------------------------------------------------------
    // Fila de espera
    // ---------------------------------------------------------------------------------------
    fun cambiarEspera(delta: Int) {
        val nuevo = (configuracion.value.clientesEnEspera + delta).coerceIn(0, 99)
        viewModelScope.launch(Dispatchers.IO) { configDao.setEspera(nuevo) }
    }
}
