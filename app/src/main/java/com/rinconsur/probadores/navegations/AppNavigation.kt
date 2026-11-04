package com.rinconsur.probadores.navegations

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rinconsur.probadores.view.ui.ConfiguracionScreen
import com.rinconsur.probadores.view.ui.HistorialScreen
import com.rinconsur.probadores.view.ui.HomeScreen
import com.rinconsur.probadores.view.ui.IncidenciaFormScreen
import com.rinconsur.probadores.view.ui.IncidenciaListScreen
import com.rinconsur.probadores.view.ui.LoginScreen
import com.rinconsur.probadores.view.ui.PersonalFormScreen
import com.rinconsur.probadores.view.ui.PersonalListScreen
import com.rinconsur.probadores.view.ui.ProbadorFormScreen
import com.rinconsur.probadores.view.ui.ProbadorListScreen
import com.rinconsur.probadores.view.utils.navEnterTransition
import com.rinconsur.probadores.view.utils.navExitTransition
import com.rinconsur.probadores.view.utils.navPopEnterTransition
import com.rinconsur.probadores.view.utils.navPopExitTransition
import com.rinconsur.probadores.viewmodels.FiltroProbadores
import com.rinconsur.probadores.viewmodels.LoginViewModel
import com.rinconsur.probadores.viewmodels.ProbadorViewModel
import kotlinx.coroutines.launch

/** Rutas de navegación */
object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val PROBADORES = "probadores"
    const val PROBADOR_FORM = "probador_form?id={id}"
    const val INCIDENCIAS = "incidencias"
    const val INCIDENCIA_FORM = "incidencia_form?id={id}&probadorId={probadorId}"
    const val HISTORIAL = "historial"
    const val PERSONAL = "personal"
    const val PERSONAL_FORM = "personal_form?id={id}"
    const val CONFIG = "configuracion"

    fun probadorForm(id: Int = 0) = "probador_form?id=$id"
    fun incidenciaForm(id: Int = 0, probadorId: Int = 0) = "incidencia_form?id=$id&probadorId=$probadorId"
    fun personalForm(id: Int = 0) = "personal_form?id=$id"
}

data class MenuItem(val route: String, val label: String, val icon: ImageVector, val soloAdmin: Boolean = false)

/** Opciones del menú lateral (Personal y Configuración solo para el Jefe de sección) */
val menuItems = listOf(
    MenuItem(Routes.HOME, "Inicio", Icons.Default.Dashboard),
    MenuItem(Routes.PROBADORES, "Probadores", Icons.Default.MeetingRoom),
    MenuItem(Routes.INCIDENCIAS, "Incidencias", Icons.Default.ReportProblem),
    MenuItem(Routes.HISTORIAL, "Historial de uso", Icons.Default.History),
    MenuItem(Routes.PERSONAL, "Personal", Icons.Default.People, soloAdmin = true),
    MenuItem(Routes.CONFIG, "Configuración", Icons.Default.Settings, soloAdmin = true)
)

private fun titleFor(route: String?): String = when (route) {
    Routes.HOME -> "Inicio"
    Routes.PROBADORES -> "Probadores"
    Routes.PROBADOR_FORM -> "Probador"
    Routes.INCIDENCIAS -> "Incidencias"
    Routes.INCIDENCIA_FORM -> "Incidencia"
    Routes.HISTORIAL -> "Historial de uso"
    Routes.PERSONAL -> "Personal"
    Routes.PERSONAL_FORM -> "Personal"
    Routes.CONFIG -> "Configuración"
    else -> ""
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel,
    probadorViewModel: ProbadorViewModel
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val loginState by loginViewModel.uiState.collectAsStateWithLifecycle()

    val isLogin = currentRoute == null || currentRoute == Routes.LOGIN
    val isTopLevel = menuItems.any { it.route == currentRoute }
    val opcionesMenu = menuItems.filter { !it.soloAdmin || loginState.esAdmin }

    fun navigateTopLevel(route: String) {
        navController.navigate(route) {
            popUpTo(Routes.HOME) { saveState = false }
            launchSingleTop = true
        }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = !isLogin,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(Modifier.height(16.dp))
                Text(
                    loginState.loggedUser?.nombre ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Text(
                    loginState.loggedUser?.email ?: "",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 2.dp)
                )
                Text(
                    loginState.rolNombre,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                opcionesMenu.forEach { item ->
                    NavigationDrawerItem(
                        label = { Text(item.label) },
                        icon = { Icon(item.icon, contentDescription = null) },
                        selected = currentRoute == item.route,
                        onClick = {
                            scope.launch { drawerState.close() }
                            if (item.route == Routes.PROBADORES) probadorViewModel.setFiltro(FiltroProbadores.TODOS)
                            navigateTopLevel(item.route)
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                NavigationDrawerItem(
                    label = { Text("Cerrar sesión") },
                    icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null) },
                    selected = false,
                    onClick = {
                        scope.launch { drawerState.close() }
                        loginViewModel.logout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(navController.graph.id) { inclusive = true }
                        }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                if (!isLogin) {
                    TopAppBar(
                        title = { Text(titleFor(currentRoute)) },
                        navigationIcon = {
                            if (isTopLevel) {
                                IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                    Icon(Icons.Default.Menu, contentDescription = "Menú")
                                }
                            } else {
                                IconButton(onClick = { navController.popBackStack() }) {
                                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        ) { innerPadding ->
            AppNavHost(
                navController = navController,
                modifier = Modifier.padding(innerPadding),
                loginViewModel = loginViewModel,
                probadorViewModel = probadorViewModel,
                esAdmin = loginState.esAdmin,
                currentUserId = loginState.loggedUser?.id,
                currentUserName = loginState.loggedUser?.nombre ?: "",
                onNavigateTopLevel = ::navigateTopLevel
            )
        }
    }
}

@Composable
private fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier,
    loginViewModel: LoginViewModel,
    probadorViewModel: ProbadorViewModel,
    esAdmin: Boolean,
    currentUserId: Int?,
    currentUserName: String,
    onNavigateTopLevel: (String) -> Unit
) {
    val idArg = navArgument("id") { type = NavType.IntType; defaultValue = 0 }
    val probadorIdArg = navArgument("probadorId") { type = NavType.IntType; defaultValue = 0 }

    // ANIMACIÓN 4 (view/utils/Animations.kt): deslizamiento horizontal entre pantallas
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN,
        modifier = modifier,
        enterTransition = navEnterTransition,
        exitTransition = navExitTransition,
        popEnterTransition = navPopEnterTransition,
        popExitTransition = navPopExitTransition
    ) {
        composable(Routes.LOGIN) {
            LoginScreen(loginViewModel) {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.LOGIN) { inclusive = true }
                }
            }
        }
        composable(Routes.HOME) {
            HomeScreen(
                userName = currentUserName,
                viewModel = probadorViewModel,
                onNavigate = onNavigateTopLevel
            )
        }
        composable(Routes.PROBADORES) {
            ProbadorListScreen(
                viewModel = probadorViewModel,
                esAdmin = esAdmin,
                onAdd = { navController.navigate(Routes.probadorForm()) },
                onEdit = { id -> navController.navigate(Routes.probadorForm(id)) },
                onReportar = { id -> navController.navigate(Routes.incidenciaForm(probadorId = id)) }
            )
        }
        composable(Routes.PROBADOR_FORM, arguments = listOf(idArg)) { entry ->
            ProbadorFormScreen(
                probadorId = entry.arguments?.getInt("id") ?: 0,
                onDone = { navController.popBackStack() }
            )
        }
        composable(Routes.INCIDENCIAS) {
            IncidenciaListScreen(
                onAdd = { navController.navigate(Routes.incidenciaForm()) },
                onEdit = { id -> navController.navigate(Routes.incidenciaForm(id = id)) }
            )
        }
        composable(Routes.INCIDENCIA_FORM, arguments = listOf(idArg, probadorIdArg)) { entry ->
            IncidenciaFormScreen(
                incidenciaId = entry.arguments?.getInt("id") ?: 0,
                probadorIdInicial = entry.arguments?.getInt("probadorId") ?: 0,
                reportadoPor = currentUserName,
                onDone = { navController.popBackStack() }
            )
        }
        composable(Routes.HISTORIAL) {
            HistorialScreen()
        }
        composable(Routes.PERSONAL) {
            PersonalListScreen(
                currentUserId = currentUserId,
                onAdd = { navController.navigate(Routes.personalForm()) },
                onEdit = { id -> navController.navigate(Routes.personalForm(id)) }
            )
        }
        composable(Routes.PERSONAL_FORM, arguments = listOf(idArg)) { entry ->
            PersonalFormScreen(
                userId = entry.arguments?.getInt("id") ?: 0,
                onDone = { navController.popBackStack() }
            )
        }
        composable(Routes.CONFIG) {
            ConfiguracionScreen()
        }
    }
}
