package com.example.tragaperras

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.Toolbar

/**
 * Menú oscuro con diseño propio: no usa la ventana nativa de overflow,
 * que en algunos temas Material3 aparece blanca con letras claras.
 *
 * Reutiliza los IDs y los títulos del R.menu.menu_main existente y
 * delega en onOptionsItemSelected() de cada Activity, manteniendo
 * exactamente su navegación (incluidos openGame, historial y salir).
 */
object YermoOverflowMenu {

    fun install(activity: AppCompatActivity, toolbar: Toolbar, screenItemId: Int) {
        val trigger = AppCompatImageButton(activity).apply {
            contentDescription = "Abrir menú de navegación"
            setImageResource(R.drawable.ic_more_vert_p2)
            setColorFilter(Color.parseColor("#FFF0D5"))
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            setPadding(dp(activity, 12), dp(activity, 12), dp(activity, 12), dp(activity, 12))
            setBackgroundResource(R.drawable.bg_overflow_trigger_p2)
            isFocusable = true
        }

        toolbar.addView(
            trigger,
            Toolbar.LayoutParams(
                dp(activity, 48),
                dp(activity, 48),
                Gravity.END or Gravity.CENTER_VERTICAL
            )
        )

        trigger.setOnClickListener { show(activity, trigger, screenItemId) }
    }

    private fun show(activity: AppCompatActivity, anchor: AppCompatImageButton, screenItemId: Int) {
        // Se usa PopupMenu SOLO como fuente de opciones; NUNCA se hace .show().
        val source = PopupMenu(activity, anchor)
        source.menuInflater.inflate(R.menu.menu_main, source.menu)

        val container = LayoutInflater.from(activity)
            .inflate(R.layout.popup_yermo_menu_p2, null) as LinearLayout
        val popupWidth = dp(activity, 214)
        val popup = PopupWindow(
            container,
            popupWidth,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        ).apply {
            isOutsideTouchable = true
            isClippingEnabled = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            elevation = dp(activity, 14).toFloat()
            inputMethodMode = PopupWindow.INPUT_METHOD_NOT_NEEDED
        }

        for (index in 0 until source.menu.size()) {
            val item = source.menu.getItem(index)
            if (!item.isVisible || item.itemId == screenItemId) continue

            val row = LayoutInflater.from(activity)
                .inflate(R.layout.popup_yermo_menu_item_p2, container, false) as TextView
            row.text = item.title
            row.contentDescription = item.title
            row.setOnClickListener {
                popup.dismiss()
                activity.onOptionsItemSelected(item)
            }
            container.addView(row)
        }

        popup.showAsDropDown(anchor, anchor.width - popupWidth, -dp(activity, 4))
    }

    private fun dp(activity: AppCompatActivity, value: Int): Int =
        (value * activity.resources.displayMetrics.density + 0.5f).toInt()
}
