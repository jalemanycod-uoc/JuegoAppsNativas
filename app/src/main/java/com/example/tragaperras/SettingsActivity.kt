package com.example.tragaperras

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Switch
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

        // Interruptor de Música
        val swMusic = findViewById<Switch>(R.id.swMusic)
        // Lo dejamos como estaba guardado (activado o desactivado)
        swMusic.isChecked = SoundManager.isMusicOn(this)
        // Cuando el jugador lo cambia
        swMusic.setOnCheckedChangeListener { _, isChecked ->
            SoundManager.setMusicOn(this, isChecked)
            if (isChecked) {
                SoundManager.playMusic(this, R.raw.musica_menu)
            } else {
                SoundManager.pauseMusic()
            }
        }

        // Interruptor de Efectos de sonido
        val swSoundEffects = findViewById<Switch>(R.id.swSoundEffects)
        swSoundEffects.isChecked = SoundManager.isEffectsOn(this)
        swSoundEffects.setOnCheckedChangeListener { _, isChecked ->
            SoundManager.setEffectsOn(this, isChecked)
        }
    }
        override fun onResume() {
            super.onResume()
            // Música del menú. Si ya sonaba continua sin empezar de cero
            SoundManager.playMusic(this, R.raw.musica_menu)
        }
        // Se avisa a soundmanager que la pantalla empieza a verse
        override fun onStart() {
            super.onStart()
            SoundManager.screenStarted()
        }
        // Se avisa a soundmanager que la pantalla deja de verse
        override fun onStop() {
            super.onStop()
            SoundManager.screenStopped()
        }


    // El menú propio reutiliza los mismos IDs y delega en este método.
    override fun onCreateOptionsMenu(menu: Menu): Boolean = false

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_home -> {
            openHome()
            true
        }
        R.id.menu_play -> {
            openExistingGameOrHome()
            true
        }
        R.id.menu_history -> {
            startActivity(Intent(this, HistoryActivity::class.java).apply {
                val fromGame = this@SettingsActivity.intent.getBooleanExtra("fromGame", false)
                putExtra("fromGame", fromGame)
                putExtra("fromHome", !fromGame)
                putExtra("playerName", this@SettingsActivity.intent.getStringExtra("playerName"))
            })
            true
        }
        R.id.menu_exit -> { finishAffinity(); true }
        else -> super.onOptionsItemSelected(item)
    }

    private fun openHome() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
        finish()
    }

    private fun openExistingGameOrHome() {
        val fromGame = intent.getBooleanExtra("fromGame", false)
        val name = intent.getStringExtra("playerName")
        if (fromGame && !name.isNullOrBlank()) {
            startActivity(Intent(this, GameActivity::class.java).apply {
                putExtra("playerName", name)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
        } else {
            // Si no había una partida abierta, pide nombre en Inicio.
            openHome()
        }
    }
}
