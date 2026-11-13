package com.example.tragaperras

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

// Pantalla de opciones. De momento solo es la interfaz:
// la música, los efectos de sonido y el idioma se programan en otros productos
class SettingsActivity : AppCompatActivity() {

    // Abre la lista de archivos de audio del móvil para que el jugador elija una canción.
    // Lo que hay entre llaves se ejecuta cuando el jugador ha elegido
    private val chooseMusicLauncher =
        registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            // uri es la dirección de la canción elegida (IMPORTANTE). Si el jugador cancela, es null
            if (uri != null) {
                // Se pide permiso para poder seguir usando la canción después de cerrar la app
                contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                SoundManager.setCustomMusic(this, uri.toString())
                SoundManager.restartMusic(this, R.raw.musica_menu)
                Toast.makeText(this, "Música cambiada", Toast.LENGTH_SHORT).show()
            }
        }

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

        // Botón Elegir música
        val btnChooseMusic = findViewById<Button>(R.id.btnChooseMusic)
        btnChooseMusic.setOnClickListener {
            // Abre la lista de archivos, y muestra únicamente los de audio
            chooseMusicLauncher.launch(arrayOf("audio/*"))
        }

        // Botón Música oficial
        val btnOfficialMusic = findViewById<Button>(R.id.btnOfficialMusic)
        btnOfficialMusic.setOnClickListener {
            // Se borra la canción elegida y luego volvemos a poner la música del juego
            SoundManager.setCustomMusic(this, null)
            SoundManager.restartMusic(this, R.raw.musica_menu)
            Toast.makeText(this, "Música oficial", Toast.LENGTH_SHORT).show()
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
