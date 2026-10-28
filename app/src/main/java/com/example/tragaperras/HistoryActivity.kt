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

class HistoryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        // Deja espacio para la barra de estado y la barra de navegación del móvil
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Usamos la Toolbar del diseño como barra de acción
        setSupportActionBar(findViewById(R.id.toolbar))

        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnBackToGame = findViewById<Button>(R.id.btnBackToGame)

        // Atrás: cierra esta pantalla y vuelve a la anterior
        btnBack.setOnClickListener { finish() }

        // Volver al juego: abre la pantalla de juego
        btnBackToGame.setOnClickListener {
            startActivity(Intent(this, GameActivity::class.java))
        }

        loadHistory()
    }

    fun loadHistory() {
        // Datos de prueba. Más adelante saldrán de SQLite
        val history = listOf(
            "27/10 17:05 | 10 | +20 | 120",
            "27/10 17:04 | 25 | 0 | 100",
            "27/10 17:02 | 50 | +75 | 125"
        )
        val lvHistory = findViewById<ListView>(R.id.lvHistory)
        lvHistory.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, history)
    }

    // Crea el menú de la barra de acción
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Qué hace cada opción del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.menu_home -> startActivity(Intent(this, MainActivity::class.java))
            R.id.menu_play -> startActivity(Intent(this, GameActivity::class.java))
            R.id.menu_exit -> finishAffinity()
        }
        return true
    }
}