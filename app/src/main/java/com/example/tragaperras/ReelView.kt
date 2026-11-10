package com.example.tragaperras

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import kotlin.math.max
import kotlin.random.Random

class ReelView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {

    private var currentImage = createImageView()
    private var incomingImage = createImageView()

    private var activeAnimator: ValueAnimator? = null
    private var currentSymbol: Symbol = Symbol.GAS_MASK

    private val symbols = Symbol.values().toList()

    init {
        clipChildren = true
        clipToPadding = true

        addView(
            currentImage,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        )

        addView(
            incomingImage,
            LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT,
                Gravity.CENTER
            )
        )

        incomingImage.visibility = View.INVISIBLE
        setSymbol(Symbol.GAS_MASK)
    }

    private fun createImageView(): ImageView =
        ImageView(context).apply {
            // Las ilustraciones nuevas casi llenan sus PNG transparentes.
            // Reducir el CONTENIDO, no las ventanas de la máquina.
            // Los dos ImageView (actual y entrante) usan la misma escala,
            // por lo que se conserva la continuidad del giro y el rebote.
            scaleType = ImageView.ScaleType.FIT_CENTER
            setPadding(0, 0, 0, 0)
            scaleX = 0.82f
            scaleY = 0.82f
        }

    fun setSymbol(symbol: Symbol) {
        currentSymbol = symbol
        currentImage.setImageResource(drawableFor(symbol))
        currentImage.translationY = 0f
        incomingImage.visibility = View.INVISIBLE
        incomingImage.translationY = 0f
    }

    fun spinTo(
        target: Symbol,
        totalDurationMs: Long,
        steps: Int,
        onEnd: () -> Unit
    ) {
        stop()

        if (height == 0) {
            post {
                spinTo(target, totalDurationMs, steps, onEnd)
            }
            return
        }

        val safeSteps = max(4, steps)

        val sequence = MutableList(safeSteps - 1) {
            randomDifferentSymbol(currentSymbol)
        }.apply {
            add(target)
        }

        val durations =
            buildDeceleratingDurations(
                totalDurationMs,
                safeSteps
            )

        animateStep(
            sequence = sequence,
            durations = durations,
            index = 0,
            onEnd = {
                currentSymbol = target
                onEnd()
            }
        )
    }

    private fun animateStep(
        sequence: List<Symbol>,
        durations: List<Long>,
        index: Int,
        onEnd: () -> Unit
    ) {
        if (index >= sequence.size) {
            onEnd()
            return
        }

        val nextSymbol = sequence[index]
        val distance = height.toFloat()

        incomingImage.setImageResource(
            drawableFor(nextSymbol)
        )

        incomingImage.translationY = -distance
        incomingImage.visibility = View.VISIBLE
        currentImage.translationY = 0f

        val animator =
            ValueAnimator.ofFloat(0f, 1f).apply {

                duration = durations[index]

                addUpdateListener { valueAnimator ->

                    val progress =
                        valueAnimator.animatedValue as Float

                    currentImage.translationY =
                        distance * progress

                    incomingImage.translationY =
                        -distance + (distance * progress)
                }

                addListener(
                    object : AnimatorListenerAdapter() {

                        override fun onAnimationEnd(
                            animation: Animator
                        ) {
                            incomingImage.translationY = 0f

                            currentImage.translationY = 0f
                            currentImage.visibility =
                                View.INVISIBLE

                            val oldCurrent = currentImage
                            currentImage = incomingImage
                            incomingImage = oldCurrent

                            currentImage.visibility =
                                View.VISIBLE

                            currentSymbol = nextSymbol

                            val isLast =
                                index == sequence.lastIndex

                            if (isLast) {
                                finalMechanicalBounce {
                                    onEnd()
                                }
                            } else {
                                animateStep(
                                    sequence,
                                    durations,
                                    index + 1,
                                    onEnd
                                )
                            }
                        }
                    }
                )
            }

        activeAnimator = animator
        animator.start()
    }

    /**
     * Pequeño rebote mecánico al detenerse.
     *
     * Se pasa unos píxeles hacia abajo y vuelve
     * al centro antes de considerar terminado
     * el rodillo.
     */
    private fun finalMechanicalBounce(
        onEnd: () -> Unit
    ) {
        val bounceDistance = dp(8).toFloat()

        currentImage.animate()
            .translationY(bounceDistance)
            .setDuration(90L)
            .withEndAction {

                currentImage.animate()
                    .translationY(-dp(3).toFloat())
                    .setDuration(80L)
                    .withEndAction {

                        currentImage.animate()
                            .translationY(0f)
                            .setDuration(110L)
                            .withEndAction {
                                onEnd()
                            }
                            .start()
                    }
                    .start()
            }
            .start()
    }

    private fun buildDeceleratingDurations(
        totalDurationMs: Long,
        steps: Int
    ): List<Long> {

        val weights =
            (0 until steps).map { i ->

                val t =
                    if (steps <= 1) {
                        1f
                    } else {
                        i.toFloat() /
                            (steps - 1).toFloat()
                    }

                0.55f + (2.9f * t * t)
            }

        val sum = weights.sum()

        return weights.map { weight ->

            max(
                38L,
                (
                    totalDurationMs *
                        (weight / sum)
                    ).toLong()
            )
        }
    }

    private fun randomDifferentSymbol(
        reference: Symbol
    ): Symbol {

        val available =
            symbols.filter {
                it != reference
            }

        return available[
            Random.nextInt(
                available.size
            )
        ]
    }

    private fun drawableFor(
        symbol: Symbol
    ): Int =
        when (symbol) {

            Symbol.GAS_MASK ->
                R.drawable.gas_mask

            Symbol.RADIOACTIVE ->
                R.drawable.radioactive

            Symbol.BURNING_EARTH ->
                R.drawable.burning_earth

            Symbol.NUCLEAR_EXPLOSION ->
                R.drawable.nuclear_explosion

            Symbol.APK_CRATE ->
                R.drawable.apk_crate

            Symbol.SKULL ->
                R.drawable.skull
        }

    fun stop() {

        activeAnimator
            ?.removeAllListeners()

        activeAnimator
            ?.cancel()

        activeAnimator = null

        currentImage.animate().cancel()
        incomingImage.animate().cancel()

        currentImage.translationY = 0f
        currentImage.visibility = View.VISIBLE

        incomingImage.translationY = 0f
        incomingImage.visibility = View.INVISIBLE
    }

    private fun dp(value: Int): Int =
        (
            value *
                resources.displayMetrics.density
            ).toInt()
}
