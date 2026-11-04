package com.rinconsur.probadores.repository.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.rinconsur.probadores.repository.models.Configuracion
import com.rinconsur.probadores.repository.models.Incidencia
import com.rinconsur.probadores.repository.models.Probador
import com.rinconsur.probadores.repository.models.RegistroUso
import com.rinconsur.probadores.repository.models.Rol
import com.rinconsur.probadores.repository.models.User

@Database(
    entities = [
        Probador::class, RegistroUso::class, Incidencia::class,
        User::class, Rol::class, Configuracion::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun probadorDao(): ProbadorDao
    abstract fun registroUsoDao(): RegistroUsoDao
    abstract fun incidenciaDao(): IncidenciaDao
    abstract fun userDao(): UserDao
    abstract fun rolDao(): RolDao
    abstract fun configuracionDao(): ConfiguracionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "probadores_database"
                )
                    // Proyecto académico (MVP): al cambiar la versión se recrea la BD
                    .fallbackToDestructiveMigration()
                    .addCallback(seedCallback)
                    .build()
                INSTANCE = instance
                instance
            }
        }

        // Instante actual en milisegundos, calculado por SQLite al crear la BD
        private const val AHORA = "(CAST(strftime('%s','now') AS INTEGER) * 1000)"

        /**
         * Datos FICTICIOS de demostración (el caso exige no usar datos reales).
         * Se cargan solo al crear la base de datos.
         */
        private val seedCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                db.execSQL(
                    "INSERT INTO roles (id, nombre) VALUES " +
                        "(1, 'Jefe de sección'), (2, 'Supervisor'), (3, 'Vendedor')"
                )
                db.execSQL(
                    "INSERT INTO users (nombre, email, password, fotoUri, rolId) VALUES " +
                        "('Carolina Rojas', 'admin@rinconsur.cl', 'admin123', NULL, 1), " +
                        "('Francisca Vega', 'supervisor@rinconsur.cl', 'super123', NULL, 2), " +
                        "('Matías Soto', 'vendedor@rinconsur.cl', 'venta123', NULL, 3)"
                )
                db.execSQL("INSERT INTO configuracion (id, limiteMinutos, clientesEnEspera) VALUES (1, 15, 2)")
                // P-02 parte ocupado hace 20 min (supera el límite => alerta de demostración)
                db.execSQL(
                    "INSERT INTO probadores (codigo, zona, estado, ocupadoDesde, prendas, alertaEnviada) VALUES " +
                        "('P-01', 'Mujer', 'LIBRE', NULL, 0, 0), " +
                        "('P-02', 'Mujer', 'OCUPADO', $AHORA - 1200000, 5, 0), " +
                        "('P-03', 'Mujer', 'LIBRE', NULL, 0, 0), " +
                        "('P-04', 'Mujer', 'MANTENCION', NULL, 0, 0), " +
                        "('P-05', 'Hombre', 'OCUPADO', $AHORA - 300000, 3, 0), " +
                        "('P-06', 'Hombre', 'LIBRE', NULL, 0, 0), " +
                        "('P-07', 'Hombre', 'LIBRE', NULL, 0, 0), " +
                        "('P-08', 'Infantil', 'LIBRE', NULL, 0, 0)"
                )
                db.execSQL(
                    "INSERT INTO registros_uso (probadorId, inicio, fin, prendas, duracionSeg) VALUES " +
                        "(1, $AHORA - 7200000, $AHORA - 6600000, 3, 600), " +
                        "(3, $AHORA - 5400000, $AHORA - 4740000, 4, 660), " +
                        "(6, $AHORA - 3600000, $AHORA - 3180000, 2, 420), " +
                        "(7, $AHORA - 2400000, $AHORA - 1500000, 5, 900), " +
                        "(1, $AHORA - 1800000, $AHORA - 1380000, 2, 420)"
                )
                db.execSQL(
                    "INSERT INTO incidencias (probadorId, descripcion, fotoUri, fecha, resuelta, reportadoPor) VALUES " +
                        "(3, 'Prendas olvidadas dentro del probador tras la salida de una clienta.', NULL, " +
                        "$AHORA - 900000, 0, 'Matías Soto')"
                )
            }
        }
    }
}
