package com.example.tragaperras

import android.view.View

/**
 * Animaciones visuales de la tragaperras.
 * No modifica el saldo ni decide el resultado de las tiradas.
 */
object UiAnimations {

    /**
     * Botón rojo: simula hundirse HACIA DENTRO de la pantalla.
     * Su centro permanece fijo (sin translationX/Y).
     * Ejecuta el giro una vez al alcanzar el tamaño mínimo.
     */
    fun pressMachineButton(
        view: View,
        onPressed: (() -> Unit)? = null
    ) {
        view.animate().cancel()

        // Evita que quede deformado si se interrumpió otra animación.
        view.translationX = 0f
        view.translationY = 0f
        view.scaleX = 1f
        view.scaleY = 1f
        view.alpha = 1f

        view.animate()
            .scaleX(0.86f)
            .scaleY(0.86f)
            .alpha(0.78f)
            .setDuration(125L)
            .withEndAction {
                onPressed?.invoke()

                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(175L)
                    .start()
            }
            .start()
    }

    /** Pulso de los rodillos ganadores. */
    fun pulseWin(view: View) {
        view.animate().cancel()
        view.animate()
            .scaleX(1.10f)
            .scaleY(1.10f)
            .alpha(0.80f)
            .setDuration(130L)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .alpha(1f)
                    .setDuration(180L)
                    .start()
            }
            .start()
    }

    /** Pulso doble de jackpot. */
    fun jackpotPulse(view: View) {
        view.animate().cancel()
        view.animate()
            .scaleX(1.13f)
            .scaleY(1.13f)
            .setDuration(120L)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(120L)
                    .withEndAction {
                        view.animate()
                            .scaleX(1.13f)
                            .scaleY(1.13f)
                            .setDuration(120L)
                            .withEndAction {
                                view.animate()
                                    .scaleX(1f)
                                    .scaleY(1f)
                                    .setDuration(140L)
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }

    /** Pequeña sacudida del saldo o del boton ante una derrota o error. */
    fun shake(view: View) {
        view.animate().cancel()
        view.animate()
            .translationX(-12f)
            .setDuration(55L)
            .withEndAction {
                view.animate()
                    .translationX(12f)
                    .setDuration(70L)
                    .withEndAction {
                        view.animate()
                            .translationX(-7f)
                            .setDuration(60L)
                            .withEndAction {
                                view.animate()
                                    .translationX(0f)
                                    .setDuration(65L)
                                    .start()
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }

    /** Efecto de pulsacion simple por compatibilidad con otras pantallas. */
    fun press(view: View) {
        view.animate().cancel()
        view.animate()
            .scaleX(0.94f)
            .scaleY(0.94f)
            .setDuration(90L)
            .withEndAction {
                view.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(100L)
                    .start()
            }
            .start()
    }

    /** Aparicion suave de un elemento. */
    fun fadeIn(view: View) {
        view.animate().cancel()
        view.alpha = 0f
        view.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .setDuration(250L)
            .start()
    }
}
