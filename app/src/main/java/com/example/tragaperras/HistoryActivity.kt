package com.example.tragaperras

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Historial real de SQLite, con una tarjeta reutilizable por tirada. */
class HistoryActivity : AppCompatActivity() {

    private lateinit var lvHistory: ListView
    private lateinit var emptyHistory: TextView
    private lateinit var repository: GameRepository
    private val disposables = CompositeDisposable()
    private val fromHome by lazy { intent.getBooleanExtra("fromHome", false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.title = ""

        repository = GameRepository(this)
        lvHistory = findViewById(R.id.lvHistory)
        emptyHistory = findViewById(R.id.tvHistoryEmpty)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnBackToGame).setOnClickListener {
            // Cierra la pantalla: vuelve al juego que ya estaba abierto,
            // sin crear otro GameActivity ni sustituir el nombre del jugador.
            finish()
        }

        if (fromHome) {
            findViewById<TextView>(R.id.tvBackToGameLabel).setText(R.string.history_back_home_p2)
            findViewById<View>(R.id.btnBackToGame).contentDescription =
                getString(R.string.history_back_home_p2)
        }
    }

    override fun onResume() {
        super.onResume()
        loadHistory()
    }

    private fun loadHistory() {
        disposables.add(repository.getHistory().subscribe(
            { games ->
                val empty = games.isEmpty()
                emptyHistory.visibility = if (empty) View.VISIBLE else View.GONE
                lvHistory.visibility = if (empty) View.GONE else View.VISIBLE
                lvHistory.adapter = HistoryRowsAdapter(games)
            },
            {
                Toast.makeText(this, R.string.history_error_p2, Toast.LENGTH_SHORT).show()
            }
        ))
    }

    private inner class HistoryRowsAdapter(games: List<GameResult>) :
        ArrayAdapter<GameResult>(this@HistoryActivity, R.layout.history_row_p2, games) {

        private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val row = convertView ?: LayoutInflater.from(context)
                .inflate(R.layout.history_row_p2, parent, false)
            val game = getItem(position) ?: return row

            val isJackpot = game.outcome == Outcome.JACKPOT
            val isLoss = game.outcome == Outcome.LOSS
            val color = Color.parseColor(
                if (isJackpot) "#FFD44E" else if (isLoss) "#FF806E" else "#B7FF70"
            )
            val background = when {
                isJackpot -> R.drawable.history_row_jackpot_p2
                isLoss -> R.drawable.history_row_loss_p2
                else -> R.drawable.history_row_win_p2
            }
            row.setBackgroundResource(background)

            row.findViewById<TextView>(R.id.tvHistoryDate).text =
                dateFormatter.format(Date(game.timestamp))

            row.findViewById<TextView>(R.id.tvHistoryOutcome).apply {
                text = getString(when {
                    isJackpot -> R.string.history_jackpot_p2
                    isLoss -> R.string.history_loss_p2
                    else -> R.string.history_win_p2
                })
                setTextColor(color)
            }

            row.findViewById<TextView>(R.id.tvHistoryPlayer).text = game.playerName

            // Beneficio NETO: el premio ya incluye la devolución de la apuesta.
            val net = game.prize - game.bet
            row.findViewById<TextView>(R.id.tvHistoryDelta).apply {
                text = getString(R.string.history_net_p2, if (net > 0) "+$net" else "$net")
                setTextColor(color)
            }

            row.findViewById<TextView>(R.id.tvHistoryBet).text =
                getString(R.string.history_bet_p2, game.bet)
            row.findViewById<TextView>(R.id.tvHistoryBalance).text =
                getString(R.string.history_balance_p2, game.balanceAfter)
            return row
        }
    }

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_home -> {
            startActivity(Intent(this, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            })
            finish()
            true
        }
        R.id.menu_play -> {
            // Conserva la solución de P1: no crear una nueva partida sin nombre.
            finish()
            true
        }
        R.id.menu_history -> true
        R.id.menu_settings -> {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
        R.id.menu_exit -> {
            finishAffinity()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }
}
