package com.example.tragaperras

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class GameActivity : AppCompatActivity() {

    private var balance = 100
    private var selectedBet = 10
    private val reelAnimator = ReelAnimator()

    private lateinit var ivReel1: ImageView
    private lateinit var ivReel2: ImageView
    private lateinit var ivReel3: ImageView
    private lateinit var ivLever: ImageView
    private lateinit var btnSpin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // NUEVO
        setContentView(R.layout.activity_game)

        // NUEVO: deja espacio para la barra de estado y la barra de navegación del móvil
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // NUEVO: usamos la Toolbar del diseño como barra de acción
        setSupportActionBar(findViewById(R.id.toolbar))

        val tvBalance = findViewById<TextView>(R.id.tvBalance)
        val rgBets = findViewById<RadioGroup>(R.id.rgBets)
        ivReel1 = findViewById(R.id.ivReel1)
        ivReel2 = findViewById(R.id.ivReel2)
        ivReel3 = findViewById(R.id.ivReel3)
        ivLever = findViewById(R.id.ivLever)
        btnSpin = findViewById(R.id.btnSpin)

        tvBalance.text = "Saldo: $balance"

        // Cuando se elige una apuesta, guardamos su valor
        rgBets.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbBet10 -> onBetSelected(10)
                R.id.rbBet25 -> onBetSelected(25)
                R.id.rbBet50 -> onBetSelected(50)
            }
        }

        // La palanca y el botón Girar hacen lo mismo
        ivLever.setOnClickListener { onLeverPulled() }
        btnSpin.setOnClickListener { onLeverPulled() }
    }

    fun onBetSelected(bet: Int) {
        selectedBet = bet
    }

    fun onLeverPulled() {
        if (selectedBet > balance) {
            Toast.makeText(this, "Saldo insuficiente", Toast.LENGTH_SHORT).show()
            return
        }

        // Desactivamos la palanca y el botón para que no se pueda girar dos veces a la vez
        ivLever.isEnabled = false
        btnSpin.isEnabled = false

        // PROVISIONAL: símbolos al azar. En el punto 6 los dará SlotMachineEngine
        val symbol1 = Symbol.values().random()
        val symbol2 = Symbol.values().random()
        val symbol3 = Symbol.values().random()

        // Los rodillos se paran de izquierda a derecha (cada uno medio segundo más tarde)
        reelAnimator.animateReel(ivReel1, symbol1, 1000) { }
        reelAnimator.animateReel(ivReel2, symbol2, 1500) { }
        reelAnimator.animateReel(ivReel3, symbol3, 2000) {
            // Cuando para el último rodillo, se puede volver a jugar
            ivLever.isEnabled = true
            btnSpin.isEnabled = true
        }
    }

    // Si se cierra la pantalla, paramos la animación
    override fun onDestroy() {
        super.onDestroy()
        reelAnimator.reset()
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
            R.id.menu_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.menu_history -> startActivity(Intent(this, HistoryActivity::class.java)) // NUEVO
            R.id.menu_exit -> finishAffinity()
        }
        return true
    }
}