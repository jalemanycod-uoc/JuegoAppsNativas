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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class GameActivity : AppCompatActivity() {

    private var balance = GameRules.INITIAL_BALANCE
    private var selectedBet = 10
    private val slotMachine = SlotMachineEngine()
    private val reelAnimator = ReelAnimator()

    private lateinit var tvBalance: TextView
    private lateinit var ivReel1: ImageView
    private lateinit var ivReel2: ImageView
    private lateinit var ivReel3: ImageView
    private lateinit var ivLever: ImageView
    private lateinit var btnSpin: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_game)

        // Deja espacio para la barra de estado y la barra de navegación del móvil
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Usamos la Toolbar del diseño como barra de acción
        setSupportActionBar(findViewById(R.id.toolbar))

        tvBalance = findViewById(R.id.tvBalance)
        val rgBets = findViewById<RadioGroup>(R.id.rgBets)
        ivReel1 = findViewById(R.id.ivReel1)
        ivReel2 = findViewById(R.id.ivReel2)
        ivReel3 = findViewById(R.id.ivReel3)
        ivLever = findViewById(R.id.ivLever)
        btnSpin = findViewById(R.id.btnSpin)

        updateBalance()

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
        // ¿Saldo suficiente?
        if (selectedBet > balance) {
            Toast.makeText(this, "Saldo insuficiente", Toast.LENGTH_SHORT).show()
            return
        }

        // Desactivamos la palanca y el botón para que no se pueda girar dos veces a la vez
        ivLever.isEnabled = false
        btnSpin.isEnabled = false

        // La máquina decide el resultado ANTES de animar,
        // así cada rodillo sabe en qué símbolo tiene que parar
        val result = slotMachine.spin(selectedBet, balance)

        // Los rodillos se paran de izquierda a derecha
        reelAnimator.animateReel(ivReel1, result.reel1, 1000) { }
        reelAnimator.animateReel(ivReel2, result.reel2, 1500) { }
        reelAnimator.animateReel(ivReel3, result.reel3, 2000) {
            // Cuando para el último rodillo, mostramos el resultado
            renderResult(result)
        }
    }

    // Actualiza el saldo y muestra el aviso que toque
    fun renderResult(result: GameResult) {
        balance = result.balanceAfter
        updateBalance()

        ivLever.isEnabled = true
        btnSpin.isEnabled = true

        when (result.outcome) {
            Outcome.JACKPOT -> showJackpotPopup(result.prize)
            Outcome.WIN -> showWinPopup(result.prize)
            Outcome.LOSS -> {
                // Si no queda saldo ni para la apuesta mínima, se acaba el juego
                if (balance < GameRules.VALID_BETS.min()) {
                    showGameOver()
                } else {
                    showLossPopup()
                }
            }
        }
    }

    private fun updateBalance() {
        tvBalance.text = "Saldo: $balance"
    }

    // ---------- Avisos (popups) ----------

    fun showWinPopup(prize: Int) {
        AlertDialog.Builder(this)
            .setTitle("Dos iguales!")
            .setMessage("Recuperas tu apuesta: +$prize monedas")
            .setPositiveButton("Continuar", null)
            .show()
    }

    fun showJackpotPopup(prize: Int) {
        AlertDialog.Builder(this)
            .setTitle("JACKPOT!")
            .setMessage("+$prize monedas")
            .setPositiveButton("Cobrar", null)
            .show()
    }

    fun showLossPopup() {
        AlertDialog.Builder(this)
            .setTitle("Has perdido")
            .setMessage("Has perdido la apuesta")
            .setPositiveButton("Continuar", null)
            .show()
    }

    fun showGameOver() {
        AlertDialog.Builder(this)
            .setTitle("Fin del juego")
            .setMessage("Te has quedado sin monedas")
            .setCancelable(false) // no se puede cerrar tocando fuera
            .setPositiveButton("Reiniciar") { _, _ ->
                balance = GameRules.INITIAL_BALANCE
                updateBalance()
            }
            .setNegativeButton("Salir") { _, _ ->
                finishAffinity()
            }
            .show()
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
            R.id.menu_history -> startActivity(Intent(this, HistoryActivity::class.java))
            R.id.menu_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.menu_exit -> finishAffinity()
        }
        return true
    }
}