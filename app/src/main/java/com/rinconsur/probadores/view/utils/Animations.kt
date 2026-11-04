package com.rinconsur.probadores.view.utils

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.navigation.NavBackStackEntry

/*
 * Animaciones de la app, reunidas en un solo lugar (IE 2.2.2).
 *   1. AnimacionEntradaFormulario -> formularios (fade + deslizamiento vertical)
 *   2. Modifier.pulsoYRotacion()  -> logo del Login (animación infinita)
 *   3. Modifier.pulsoAlerta()     -> ícono de un probador en alerta (retroalimentación de urgencia)
 *   4. Transiciones de navegación -> NavHost (deslizamiento horizontal entre pantallas)
 *   Además, en las pantallas: animateColorAsState (cambio de color de estado), animateIntAsState
 *   (contadores del panel), AnimatedContent (fila de espera) y animateContentSize (tarjetas).
 */

// ---------------------------------------------------------------------------
// 1) Entrada de formularios
// ---------------------------------------------------------------------------
@Composable
fun AnimacionEntradaFormulario(
    visible: Boolean,
    content: @Composable AnimatedVisibilityScope.() -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(tween(500)) { it / 2 },
        content = content
    )
}

// ---------------------------------------------------------------------------
// 2) Pulso de escala + rotación suave (logo del Login)
// ---------------------------------------------------------------------------
@Composable
fun Modifier.pulsoYRotacion(): Modifier {
    val transition = rememberInfiniteTransition(label = "pulsoYRotacion")
    val scale by transition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale"
    )
    val rotation by transition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Reverse),
        label = "rotation"
    )
    return this.graphicsLayer {
        scaleX = scale
        scaleY = scale
        rotationZ = rotation
    }
}

// ---------------------------------------------------------------------------
// 3) Pulso de alerta (escala + transparencia) para probadores con uso prolongado
// ---------------------------------------------------------------------------
@Composable
fun Modifier.pulsoAlerta(): Modifier {
    val transition = rememberInfiniteTransition(label = "pulsoAlerta")
    val escala by transition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(600, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "escalaAlerta"
    )
    val alfa by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
        label = "alfaAlerta"
    )
    return this.graphicsLayer {
        scaleX = escala
        scaleY = escala
        alpha = alfa
    }
}

// ---------------------------------------------------------------------------
// 4) Transiciones de navegación (NavHost)
// ---------------------------------------------------------------------------
private const val DURACION_NAV = 400

val navEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION_NAV)) +
        fadeIn(tween(DURACION_NAV))
}

val navExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Left, tween(DURACION_NAV)) +
        fadeOut(tween(DURACION_NAV))
}

val navPopEnterTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> EnterTransition = {
    slideIntoContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION_NAV)) +
        fadeIn(tween(DURACION_NAV))
}

val navPopExitTransition: AnimatedContentTransitionScope<NavBackStackEntry>.() -> ExitTransition = {
    slideOutOfContainer(AnimatedContentTransitionScope.SlideDirection.Right, tween(DURACION_NAV)) +
        fadeOut(tween(DURACION_NAV))
}
