package com.example.tragaperras

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
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

class HistoryActivity : AppCompatActivity() {

    private lateinit var lvHistory: ListView
    private lateinit var repository: GameRepository
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

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

        // Se usa la Toolbar del diseño como barra de acción
        setSupportActionBar(findViewById(R.id.toolbar))

        repository = GameRepository(this)

        lvHistory = findViewById(R.id.lvHistory)

        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnBackToGame = findViewById<Button>(R.id.btnBackToGame)

        // Atrás: cierra esta pantalla y vuelve a la pantalla anterior
        btnBack.setOnClickListener {
            finish()
        }

        /*
         * Modificación Pere 28/10.
         *
         * Antes se creaba una nueva GameActivity al pulsar
         * "Volver al juego".
         *
         * Esa nueva Activity no recibía el nombre del jugador,
         * por lo que utilizaba el valor por defecto "Jugador".
         * Las tiradas posteriores podían guardarse en SQLite
         * con un nombre incorrecto.
         *
         * Ahora se cierra HistoryActivity con finish() y se vuelve
         * a la GameActivity que ya estaba abierta.
         *
         * De esta forma se conservan el nombre del jugador,
         * el saldo y el estado actual de la partida.
         */
        btnBackToGame.setOnClickListener {
            finish()
        }
    }

    // Cada vez que se ve la pantalla, recargamos el historial
    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    // Lee todas las tiradas de la base de datos mediante RxJava
    // y las muestra en el ListView
    fun loadHistory() {

        disposables.add(

            repository.getHistory().subscribe(

                { games ->

                    val dateFormat =
                        SimpleDateFormat(
                            "dd/MM HH:mm",
                            Locale.getDefault()
                        )

                    // Convierte cada tirada en una línea de texto
                    val lines = mutableListOf<String>()

                    for (game in games) {

                        val date =
                            dateFormat.format(
                                Date(game.timestamp)
                            )

                        val prize =
                            if (game.prize > 0) {
                                "+${game.prize}"
                            } else {
                                "0"
                            }

                        lines.add(
                            "${game.playerName} | " +
                                    "$date | " +
                                    "${game.bet} | " +
                                    "$prize | " +
                                    "${game.balanceAfter}"
                        )
                    }

                    if (lines.isEmpty()) {
                        lines.add(
                            "Todavía no hay partidas"
                        )
                    }

                    lvHistory.adapter =
                        ArrayAdapter(
                            this,
                            android.R.layout.simple_list_item_1,
                            lines
                        )
                },

                {
                    Toast.makeText(
                        this,
                        "Error al leer el historial",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        )
    }

    // Al cerrar la pantalla se cancelan las llamadas de RxJava
    override fun onDestroy() {
        super.onDestroy()
        disposables.clear()
    }

    // Crea el menú de la barra de acción
    override fun onCreateOptionsMenu(menu: Menu): Boolean {

        menuInflater.inflate(
            R.menu.menu_main,
            menu
        )

        return true
    }

    // Define qué hace cada opción del menú
    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {

            // Volver a la pantalla de inicio
            R.id.menu_home -> {

                startActivity(
                    Intent(
                        this,
                        MainActivity::class.java
                    )
                )
            }

            /*
             * Modificación Pere 29/10.
             *
             * Antes esta opción también creaba una nueva GameActivity
             * sin transmitir el nombre del jugador.
             *
             * Ahora simplemente se cierra HistoryActivity y se vuelve
             * a la GameActivity existente.
             */
            R.id.menu_play -> {
                finish()
            }

            // Abrir ajustes
            R.id.menu_settings -> {

                startActivity(
                    Intent(
                        this,
                        SettingsActivity::class.java
                    )
                )
            }

            // Cerrar la aplicación
            R.id.menu_exit -> {
                finishAffinity()
            }
        }

        return true
    }
}