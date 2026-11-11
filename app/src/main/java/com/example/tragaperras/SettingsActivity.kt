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
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Usamos la Toolbar del diseño como barra de acción
        val yermoToolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(yermoToolbar)
        YermoOverflowMenu.install(this, yermoToolbar, R.id.menu_settings)

        // Atrás: cierra esta pantalla y vuelve a la anterior
        val btnBack = findViewById<Button>(R.id.btnBack)
        btnBack.setOnClickListener { finish() }
    }

    // Crea el menú de la barra de acción
    // El menú blanco de AppCompat queda desactivado: lo sustituye YermoOverflowMenu.
    override fun onCreateOptionsMenu(menu: Menu): Boolean = false

    // Qué hace cada opción del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_home -> startActivity(Intent(this, MainActivity::class.java))
            R.id.menu_play -> startActivity(Intent(this, GameActivity::class.java))
            R.id.menu_history -> startActivity(Intent(this, HistoryActivity::class.java))
            R.id.menu_exit -> finishAffinity()
        }
        return true
    }
}