package com.example.tragaperras

import android.os.CountDownTimer
import android.widget.ImageView

// Hace girar un rodillo: cambia la imagen muy rápido
// y al final deja el símbolo que ha tocado
class ReelAnimator {

    // Guardamos los temporizadores para poder pararlos
    private val timers = mutableListOf<CountDownTimer>()

    fun animateReel(view: ImageView, finalSymbol: Symbol, durationMs: Long, onEnd: () -> Unit) {
        // onTick se ejecuta cada 80 milisegundos hasta que pasa durationMs
        val timer = object : CountDownTimer(durationMs, 80) {

            override fun onTick(millisUntilFinished: Long) {
                // Ponemos un símbolo al azar para que parezca que gira
                view.setImageResource(Symbol.values().random().image)
            }

            override fun onFinish() {
                // El rodillo se para en el símbolo que ha tocado
                view.setImageResource(finalSymbol.image)
                onEnd()
            }
        }
        timers.add(timer)
        timer.start()
    }

    // Para todos los rodillos (por ejemplo, si se cierra la pantalla)
    fun reset() {
        for (timer in timers) {
            timer.cancel()
        }
        timers.clear()
    }
}