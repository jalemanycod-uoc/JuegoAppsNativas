package com.example.tragaperras

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // NUEVO
        setContentView(R.layout.activity_main)

        // NUEVO: deja espacio para la barra de estado y la barra de navegación del móvil
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // NUEVO: usamos la Toolbar del diseño como barra de acción
        setSupportActionBar(findViewById(R.id.toolbar))

        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnHistory = findViewById<Button>(R.id.btnHistory)
        val lvBestScores = findViewById<ListView>(R.id.lvBestScores)

        // Datos de prueba. Más adelante saldrán de SQLite
        val bestScores = listOf(
            "27/10/2026 17:05 - 450 monedas",
            "26/10/2026 19:12 - 320 monedas"
        )
        lvBestScores.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bestScores)

        // El botón Jugar abre la pantalla de juego
        btnPlay.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        // NUEVO: el botón Historial abre la pantalla de historial
        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    // Crea el menú de la barra de acción
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Qué hace cada opción del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_play -> startActivity(Intent(this, GameActivity::class.java))
            R.id.menu_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.menu_history -> startActivity(Intent(this, HistoryActivity::class.java)) // NUEVO
            R.id.menu_exit -> finishAffinity()
        }
        return true
    }
}