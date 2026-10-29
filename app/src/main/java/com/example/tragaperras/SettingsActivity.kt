package com.example.tragaperras

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// Pantalla de opciones. De momento solo es la interfaz:
// la música, los efectos de sonido y el idioma se programan en otros productos
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)

        // Deja espacio para la barra de estado y la barra de navegación del móvil
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->

            val systemBars =
                insets.getInsets(WindowInsetsCompat.Type.systemBars())

            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )

            insets
        }

        // Usamos la Toolbar del diseño como barra de acción
        setSupportActionBar(findViewById(R.id.toolbar))

        // Atrás: cierra esta pantalla y vuelve a la anterior
        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            finish()
        }
    }

    // Crea el menú de la barra de acción
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(
            R.menu.menu_main,
            menu
        )

        return true
    }

    // Qué hace cada opción del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            // Volver a Inicio
            R.id.menu_home -> {

                val intent =
                    Intent(this, MainActivity::class.java)

                intent.addFlags(
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                            Intent.FLAG_ACTIVITY_SINGLE_TOP
                )

                startActivity(intent)
            }

            /*
             * Modificación Pere 29/10
             * Antes se creaba una nueva GameActivity mediante startActivity().
             * Esa nueva pantalla no recibía el nombre del jugador y podía
             * utilizar el valor por defecto "Jugador".
             *
             * Ahora se cierra SettingsActivity para volver a la Activity
             * anterior, evitando crear una nueva instancia del juego.
             *
             * Si Opciones se abrió desde una partida, se recupera la misma
             * GameActivity con el nombre, saldo y estado que ya tenía.
             */
            R.id.menu_play -> {
                finish()
            }

            // Abrir Historial
            R.id.menu_history -> {

                startActivity(
                    Intent(
                        this,
                        HistoryActivity::class.java
                    )
                )
            }

            // Cerrar completamente la aplicación
            R.id.menu_exit -> {
                finishAffinity()
            }
        }

        return true
    }
}