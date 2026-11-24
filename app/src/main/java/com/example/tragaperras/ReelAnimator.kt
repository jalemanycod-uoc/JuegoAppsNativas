package com.example.tragaperras

/**
 * Coordina la animación de cada ReelView.
 *
 * Conserva la idea del P1:
 * - rodillo 1 se detiene antes;
 * - rodillo 2 después;
 * - rodillo 3 el último.
 *
 * Cada ReelView realiza desplazamiento vertical real
 * y desacelera progresivamente.
 */
class ReelAnimator {

    private val activeReels = mutableSetOf<ReelView>()

    fun animateReel(
        reel: ReelView,
        target: Symbol,
        durationMs: Long,
        onEnd: () -> Unit
    ) {
        activeReels.add(reel)

        val steps = when {
            durationMs <= 1000L -> 10
            durationMs <= 1500L -> 13
            else -> 16
        }

        reel.spinTo(
            target = target,
            totalDurationMs = durationMs,
            steps = steps
        ) {
            activeReels.remove(reel)
            onEnd()
        }
    }

    fun reset() {
        activeReels.forEach { it.stop() }
        activeReels.clear()
    }
}
