package com.example.tragaperras

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var lvBestScores: ListView
    private lateinit var repository: GameRepository
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Deja espacio para la barra de estado y la barra de navegación del móvil
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemBars.left,
                systemBars.top,
                systemBars.right,
                systemBars.bottom
            )
            insets
        }

        // Hemos usado la Toolbar del diseño como barra de acción
        setSupportActionBar(findViewById(R.id.toolbar))

        repository = GameRepository(this)

        etName = findViewById(R.id.etName)
        lvBestScores = findViewById(R.id.lvBestScores)

        val btnPlay = findViewById<Button>(R.id.btnPlay)
        val btnHistory = findViewById<Button>(R.id.btnHistory)

        // El botón Jugar abre la pantalla de juego con el nombre escrito
        btnPlay.setOnClickListener {
            openGame()
        }

        // El botón Historial abre la pantalla de historial
        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
    }

    // onResume se ejecuta cada vez que la pantalla vuelve a verse,
    // por ejemplo al volver del juego, así las puntuaciones están siempre al día
    override fun onResume() {
        super.onResume()
        loadBestScores()
    }

    fun openGame() {
        val name = etName.text.toString().trim()

        // Si no hay nombre, se avisa y no se abre el juego
        if (name.isEmpty()) {
            etName.error = "Escribe tu nombre para jugar"
            return
        }

        /*
         * Modificación Pere 29/11.
         *
         * Antes se llamaba a repository.startNewGame() cada vez que el jugador
         * pulsaba "Jugar". Esa llamada reiniciaba el saldo almacenado en SQLite
         * al saldo inicial de 250 monedas.
         *
         * Se elimina esa llamada para que, si el usuario cierra la aplicación
         * y vuelve a entrar, GameActivity recupere el saldo que ya estaba
         * almacenado en la base de datos.
         *
         * Una nueva partida con 250 monedas seguirá comenzando cuando el jugador
         * llegue a Game Over y pulse "Reiniciar".
         */
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("playerName", name)
        startActivity(intent)
    }

    // Lee las 5 mejores partidas de la base de datos (con RxJava) y las muestra
    private fun loadBestScores() {
        disposables.add(
            repository.getBestResults().subscribe(
                { games ->
                    val dateFormat =
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

                    // Convierte cada partida en una línea de texto
                    val lines = mutableListOf<String>()

                    for (game in games) {
                        val date = dateFormat.format(Date(game.timestamp))

                        lines.add(
                            "${game.playerName} - $date - máximo " +
                                    "${game.balanceAfter} monedas"
                        )
                    }

                    if (lines.isEmpty()) {
                        lines.add("Todavía no hay partidas")
                    }

                    lvBestScores.adapter =
                        ArrayAdapter(
                            this,
                            android.R.layout.simple_list_item_1,
                            lines
                        )
                },
                {
                    Toast.makeText(
                        this,
                        "Error al leer las puntuaciones",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        )
    }

    // Al cerrar la pantalla, se cancelan las llamadas de RxJava
    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    // Crea el menú de la barra de acción
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    // Las opciones del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.menu_play ->
                openGame()

            R.id.menu_history ->
                startActivity(
                    Intent(this, HistoryActivity::class.java)
                )

            R.id.menu_settings ->
                startActivity(
                    Intent(this, SettingsActivity::class.java)
                )

            R.id.menu_exit ->
                finishAffinity()
        }

        return true
    }
}