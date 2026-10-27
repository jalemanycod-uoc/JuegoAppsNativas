package com.example.tragaperras

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnHistory = findViewById<Button>(R.id.btnHistory)
        val lvBestScores = findViewById<ListView>(R.id.lvBestScores)

        // Datos de prueba. Más adelante saldrán de SQLite
        val bestScores = listOf(
            "27/10/2026 17:05 - 450 monedas",
            "26/10/2026 19:12 - 320 monedas"
        )
        lvBestScores.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, bestScores)

        btnPlay.setOnClickListener {
            // Lo programaremos cuando creemos la pantalla de juego
        }

        btnHistory.setOnClickListener {
            // Lo programaremos cuando creemos la pantalla de historial
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
            R.id.menu_exit -> finishAffinity()
        }
        return true
    }
}