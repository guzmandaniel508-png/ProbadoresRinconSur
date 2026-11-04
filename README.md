# Probadores Rincón Sur

App Android (Kotlin + Jetpack Compose + Material Design 3) para la **gestión interna de ocupación de probadores**
del personal de la Tienda Departamental Rincón Sur (caso DSY1105, Evaluación Parcial 2).

> **Integrantes:** _Alex Daniel Guzman Gonzalez_ · _Luis Felipe Cid_   |   **Equipo N°:** _18-C_   |   **Docente:** _Sergio Eduardo Fuentes Perez_

## Problema que resuelve
El personal hoy debe recorrer el pasillo para saber qué probadores están ocupados, sin registro de tiempos ni aviso de
prendas abandonadas. La app centraliza el estado de cada probador, mide cuánto tiempo lleva ocupado, avisa cuando supera
un tiempo máximo, lleva la fila de clientes en espera y estima la espera.

## Abrir y ejecutar
1. Android Studio → *Open* → seleccionar la carpeta `ProbadoresRinconSur`.
2. Esperar el *Gradle Sync* (Gradle 8.13, AGP 8.13.1, Kotlin 2.0.21, JDK 17).
3. Ejecutar en emulador o dispositivo Android (minSdk 26). Para la cámara se recomienda un dispositivo/emulador con cámara.
4. Usuarios de prueba (datos ficticios):

| Rol | Correo | Contraseña |
|---|---|---|
| Jefe de sección (administra todo) | admin@rinconsur.cl | admin123 |
| Supervisor | supervisor@rinconsur.cl | super123 |
| Vendedor | vendedor@rinconsur.cl | venta123 |

Al instalar se cargan 8 probadores de ejemplo; **P-02 parte ocupado hace 20 min** para ver de inmediato una alerta.

## Funcionalidades
- Login contra Room con validación por campo.
- **Panel de inicio:** contadores animados (libres / ocupados / en alerta / mantención), fila de espera con +/− y espera estimada.
- **Probadores:** tarjetas por color de estado, cronómetro en vivo con barra hacia el límite, ocupar (con cantidad de prendas), liberar, mantención y filtros.
- **Alertas:** al superar el tiempo máximo se envía notificación + vibración y la tarjeta pulsa.
- **Incidencias** (prenda abandonada): formulario con foto de evidencia tomada con la cámara; marcar resuelta, editar, eliminar.
- **Historial de uso** con duración promedio (alimenta la espera estimada).
- **Personal** (CRUD con foto de perfil) y **Configuración** del tiempo máximo: solo Jefe de sección.

## Requisitos de la rúbrica → dónde están
| Indicador | Implementación |
|---|---|
| IE 2.1.1 Interfaz y navegación | `navegations/AppNavigation.kt` (menú lateral + NavHost con argumentos), `view/ui/*`, `view/theme/Theme.kt` |
| IE 2.1.2 Formularios con validación e íconos | `view/utils/Campos.kt` (ícono inicial, borde/ícono/mensaje de error, ícono verde de válido). Formularios: Login, Probador, Incidencia, Personal, Configuración, Ocupar |
| IE 2.2.1 Validación desacoplada | `viewmodels/Validaciones.kt` + `*FormViewModel` (la UI solo muestra el error del estado). Reglas de negocio en `ReglasProbadores.kt`. Tests en `app/src/test` |
| IE 2.2.2 Animaciones | `view/utils/Animations.kt`: entrada de formularios, pulso del logo, pulso de alerta, transiciones de navegación; además `animateColorAsState`, `animateIntAsState`, `AnimatedContent`, `animateContentSize` |
| IE 2.3.1 Modularidad + persistencia | MVVM (`repository` / `viewmodels` / `view`), Room con 6 tablas y claves foráneas, corrutinas y Flow |
| IE 2.3.2 GitHub y Trello | Ver `GUIA_ENTREGA.md` |
| IE 2.4.1 Recursos nativos (≥2) | **Cámara** (CameraX, `CameraCapture.kt`), **Notificaciones** (`NotificacionHelper.kt`, permiso `POST_NOTIFICATIONS`), **Vibración** (`VibracionHelper.kt`) |

## Estructura
```
app/src/main/java/com/rinconsur/probadores/
├── MainActivity.kt
├── repository/
│   ├── data/      AppDatabase, Converters, ProbadorDao, RegistroUsoDao, IncidenciaDao, UserDao, RolDao, ConfiguracionDao
│   └── models/    Probador, EstadoProbador, RegistroUso, Incidencia, User, Rol, Configuracion, Relaciones
├── recursos/      NotificacionHelper, VibracionHelper            (recursos nativos)
├── viewmodels/    Login, Probador, ProbadorForm, Incidencia, IncidenciaForm, Personal, PersonalForm,
│                  Configuracion, Historial, Validaciones, ReglasProbadores
├── navegations/   AppNavigation.kt
└── view/
    ├── theme/     Theme.kt
    ├── utils/     Animations, CameraCapture, PhotoBox, Campos, Cronometro, Formato
    └── ui/        Login, Home, ProbadorList/Form, IncidenciaList/Form, PersonalList/Form, Configuracion, Historial
```

## Alcance y limitaciones (MVP)
- Datos **ficticios**; contraseñas en texto plano solo con fines académicos.
- Sin sensores: el estado se registra manualmente. Quedó preparado para recibir un dato de ocupación externo (backend Spring Boot + Retrofit en la siguiente etapa).
- Las alertas se detectan mientras el proceso de la app está activo; una versión productiva usaría WorkManager/servicio o notificaciones push desde el backend.
- Fotos en almacenamiento interno privado (`files/fotos/...`), excluidas del respaldo.
