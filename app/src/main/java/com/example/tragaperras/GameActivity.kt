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
import io.reactivex.rxjava3.disposables.CompositeDisposable

class GameActivity : AppCompatActivity() {

    private var balance = GameRules.INITIAL_BALANCE
    private var selectedBet = 10
    private var playerName = "Jugador"
    private val slotMachine = SlotMachineEngine()
    private val reelAnimator = ReelAnimator()

    // Acceso a la base de datos
    private lateinit var repository: GameRepository
    // Guarda las llamadas de RxJava para cancelarlas al cerrar la pantalla
    private val disposables = CompositeDisposable()

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

        // Nombre del jugador que llega desde la pantalla de bienvenida
        val nameFromWelcome = intent.getStringExtra("playerName")
        if (!nameFromWelcome.isNullOrBlank()) {
            playerName = nameFromWelcome
        }
        supportActionBar?.title = "Partida de $playerName"

        tvBalance = findViewById(R.id.tvBalance)
        val rgBets = findViewById<RadioGroup>(R.id.rgBets)
        ivReel1 = findViewById(R.id.ivReel1)
        ivReel2 = findViewById(R.id.ivReel2)
        ivReel3 = findViewById(R.id.ivReel3)
        ivLever = findViewById(R.id.ivLever)
        btnSpin = findViewById(R.id.btnSpin)

        updateBalance()

        // Lectura del saldo guardado en la base de datos
        repository = GameRepository(this)
        disposables.add(
            repository.getBalance().subscribe(
                { savedBalance ->
                    balance = savedBalance
                    updateBalance()
                },
                { error ->
                    Toast.makeText(this, "Error al leer el saldo", Toast.LENGTH_SHORT).show()
                }
            )
        )

        // Cuando se elige una apuesta, se guarda su valor
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

        // Desactivar la palanca y el botón para que no se pueda girar dos veces a la vez
        ivLever.isEnabled = false
        btnSpin.isEnabled = false

        // Copy() hace una copia del resultado añadiéndole el nombre del jugador
        val result = slotMachine.spin(selectedBet, balance).copy(playerName = playerName)

        // Los rodillos se paran de izquierda a derecha
        reelAnimator.animateReel(ivReel1, result.reel1, 1000) { }
        reelAnimator.animateReel(ivReel2, result.reel2, 1500) { }
        reelAnimator.animateReel(ivReel3, result.reel3, 2000) {
            // Cuando para el último rodillo, mostramos y guardamos el resultado
            renderResult(result)
            saveResult(result)
        }
    }

    // Se guarda la partida y el saldo acumulado en la base de datos (con RxJava)
    private fun saveResult(result: GameResult) {
        disposables.add(
            repository.saveGame(result).subscribe(
                { },
                { error ->
                    Toast.makeText(this, "Error al guardar la partida", Toast.LENGTH_SHORT).show()
                }
            )
        )
        disposables.add(
            repository.saveBalance(result.balanceAfter).subscribe(
                { },
                { error ->
                    Toast.makeText(this, "Error al guardar el saldo", Toast.LENGTH_SHORT).show()
                }
            )
        )
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

    // Avisos

    fun showWinPopup(prize: Int) {
        AlertDialog.Builder(this)
            .setTitle("¡Dos iguales!")
            .setMessage("Recuperas tu apuesta: +$prize monedas")
            .setPositiveButton("Continuar", null)
            .show()
    }

    fun showJackpotPopup(prize: Int) {
        AlertDialog.Builder(this)
            .setTitle("¡JACKPOT!")
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
                // Se guarda el saldo inicial en la base de datos
                disposables.add(repository.resetBalance().subscribe({ }, { }))
            }
            .setNegativeButton("Salir") { _, _ ->
                finishAffinity()
            }
            .show()
    }

    // Si se cierra la pantalla, se para la animación y las llamadas de RxJava
    override fun onDestroy() {
        super.onDestroy()
        reelAnimator.reset()
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
            R.id.menu_home -> startActivity(Intent(this, MainActivity::class.java))
            R.id.menu_history -> startActivity(Intent(this, HistoryActivity::class.java))
            R.id.menu_settings -> startActivity(Intent(this, SettingsActivity::class.java))
            R.id.menu_exit -> finishAffinity()
        }
        return true
    }
}