package com.example.tragaperras

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.EditText
import android.widget.LinearLayout
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

class MainActivity : AppCompatActivity() {

    private lateinit var etName: EditText
    private lateinit var tvNameError: TextView
    private lateinit var scoresContainer: LinearLayout
    private lateinit var repository: GameRepository
    private val disposables = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        // Barra transparente flotante: conserva únicamente los tres puntos.
        val yermoToolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(yermoToolbar)
        YermoOverflowMenu.install(this, yermoToolbar, R.id.menu_home)
        supportActionBar?.title = ""

        // Evita que el teclado aparezca al abrir Inicio y deja desplazar la lista.
        window.setSoftInputMode(
            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN or
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
        )

        repository = GameRepository(this)
        etName = findViewById(R.id.etName)
        tvNameError = findViewById(R.id.tvNameError)
        scoresContainer = findViewById(R.id.scoresContainer)

        etName.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) = Unit
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (!s.isNullOrBlank()) tvNameError.visibility = View.GONE
            }
            override fun afterTextChanged(s: Editable?) = Unit
        })

        findViewById<View>(R.id.btnPlay).setOnClickListener { openGame() }
        findViewById<View>(R.id.btnHistory).setOnClickListener { openHistory() }
    }

    override fun onResume() {
        super.onResume()
        loadBestScores()
        // música del menú
        SoundManager.playMusic(this, R.raw.musica_menu)
    }

    // Avisamos a SoundManager que esta pantalla empieza a verse
    override fun onStart() {
        super.onStart()
        SoundManager.screenStarted()
    }

    // Avisamos a SoundManager que esta pantalla deja de verse
    override fun onStop() {
        super.onStop()
        SoundManager.screenStopped()
    }

    fun openGame() {
        val name = etName.text.toString().trim()
        if (name.isEmpty()) {
            // No usar etName.error: el aviso flotante tapaba la placa y JUGAR.
            tvNameError.visibility = View.VISIBLE
            etName.requestFocus()
            return
        }
        tvNameError.visibility = View.GONE
        // NO iniciar una partida nueva ni reiniciar el saldo al navegar.
        startActivity(Intent(this, GameActivity::class.java).apply {
            putExtra("playerName", name)
        })
    }

    private fun openHistory() {
        startActivity(Intent(this, HistoryActivity::class.java).apply {
            putExtra("fromHome", true)
        })
    }

    private fun loadBestScores() {
        disposables.add(repository.getBestResults().subscribe(
            { games -> renderBestScores(games) },
            {
                Toast.makeText(this, R.string.home_scores_error, Toast.LENGTH_SHORT).show()
            }
        ))
    }

    private fun renderBestScores(games: List<GameResult>) {
        scoresContainer.removeAllViews()
        if (games.isEmpty()) {
            val empty = TextView(this).apply {
                text = getString(R.string.home_scores_empty)
                setTextColor(android.graphics.Color.parseColor("#F1ECD7"))
                textSize = 15f
                setPadding(dp(14), dp(12), dp(14), dp(18))
            }
            scoresContainer.addView(empty)
            return
        }

        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val inflater = LayoutInflater.from(this)
        games.take(5).forEachIndexed { index, result ->
            val row = inflater.inflate(R.layout.home_score_row_p2, scoresContainer, false)
            row.findViewById<TextView>(R.id.tvScoreRank).text = (index + 1).toString()
            row.findViewById<TextView>(R.id.tvScorePlayer).text = result.playerName
            row.findViewById<TextView>(R.id.tvScoreDate).text = formatter.format(Date(result.timestamp))
            row.findViewById<TextView>(R.id.tvScoreCoins).text =
                getString(R.string.home_score_coins, result.balanceAfter)
            scoresContainer.addView(row)
        }
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        disposables.clear()
        super.onDestroy()
    }

    // El menú blanco de AppCompat queda desactivado: lo sustituye YermoOverflowMenu.
    override fun onCreateOptionsMenu(menu: Menu): Boolean = false

    override fun onOptionsItemSelected(item: MenuItem): Boolean = when (item.itemId) {
        R.id.menu_play -> { openGame(); true }
        R.id.menu_history -> { openHistory(); true }
        R.id.menu_settings -> {
            startActivity(Intent(this, SettingsActivity::class.java))
            true
        }
        R.id.menu_exit -> { finishAffinity(); true }
        else -> super.onOptionsItemSelected(item)
    }
}
