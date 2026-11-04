package com.rinconsur.probadores

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.rinconsur.probadores.navegations.AppNavigation
import com.rinconsur.probadores.recursos.NotificacionHelper
import com.rinconsur.probadores.view.theme.AppTheme
import com.rinconsur.probadores.viewmodels.LoginViewModel
import com.rinconsur.probadores.viewmodels.ProbadorViewModel

class MainActivity : ComponentActivity() {

    // ViewModels compartidos (alcance de la Activity). Los de formularios y listas
    // secundarias se crean por pantalla con viewModel() dentro de cada Composable.
    private val loginViewModel: LoginViewModel by viewModels()
    private val probadorViewModel: ProbadorViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        NotificacionHelper.crearCanal(this)
        enableEdgeToEdge()
        setContent {
            AppTheme {
                AppNavigation(loginViewModel, probadorViewModel)
            }
        }
    }
}
