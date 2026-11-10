package com.example.tragaperras

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
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

    private lateinit var repository: GameRepository
    private val disposables = CompositeDisposable()

    private lateinit var tvBalance: TextView
    private lateinit var reelArea: View
    private lateinit var ivReel1: ReelView
    private lateinit var ivReel2: ReelView
    private lateinit var ivReel3: ReelView
    private lateinit var btnMachine: ImageView

    // Blanco en reposo; rojo al gastar/perder; verde al ganar saldo neto.
    private val balanceNeutralColor = Color.parseColor("#FFFFFF")
    private val balanceNegativeColor = Color.parseColor("#E55252")
    private val balancePositiveColor = Color.parseColor("#76D65B")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContentView(R.layout.activity_game)

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

        setSupportActionBar(findViewById(R.id.toolbar))

        val nameFromWelcome = intent.getStringExtra("playerName")
        if (!nameFromWelcome.isNullOrBlank()) {
            playerName = nameFromWelcome
        }
        supportActionBar?.title = "Partida de $playerName"

        tvBalance = findViewById(R.id.tvBalance)
        reelArea = findViewById(R.id.reelArea)
        ivReel1 = findViewById(R.id.ivReel1)
        ivReel2 = findViewById(R.id.ivReel2)
        ivReel3 = findViewById(R.id.ivReel3)
        btnMachine = findViewById(R.id.btnMachine)
        val rgBets = findViewById<RadioGroup>(R.id.rgBets)

        updateBalanceNeutral()
        repository = GameRepository(this)

        disposables.add(
            repository.getBalance().subscribe(
                { savedBalance ->
                    balance = savedBalance
                    updateBalanceNeutral()
                    if (balance < GameRules.VALID_BETS.min()) {
                        showGameOverDialog()
                    }
                },
                {
                    Toast.makeText(this, "Error al leer el saldo", Toast.LENGTH_SHORT).show()
                }
            )
        )

        rgBets.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbBet10 -> onBetSelected(10)
                R.id.rbBet25 -> onBetSelected(25)
                R.id.rbBet50 -> onBetSelected(50)
            }
        }

        btnMachine.setOnClickListener {
            if (!btnMachine.isEnabled) return@setOnClickListener
            UiAnimations.pressMachineButton(btnMachine) {
                onMachineButtonPressed()
            }
        }
    }

    fun onBetSelected(bet: Int) {
        selectedBet = bet
    }

    fun onMachineButtonPressed() {
        if (selectedBet > balance) {
            Toast.makeText(this, "Saldo insuficiente", Toast.LENGTH_SHORT).show()
            UiAnimations.shake(btnMachine)
            return
        }

        btnMachine.isEnabled = false

        // Previsualización: se descuenta visualmente la apuesta, sin cambiar
        // el saldo real hasta que se obtiene el resultado de SlotMachineEngine.
        updateBalanceDisplay(balance - selectedBet, balanceNegativeColor)

        val result = slotMachine.spin(selectedBet, balance)
            .copy(playerName = playerName)

        reelAnimator.animateReel(ivReel1, result.reel1, 1400L) { }
        reelAnimator.animateReel(ivReel2, result.reel2, 1900L) { }
        reelAnimator.animateReel(ivReel3, result.reel3, 2500L) {
            // Dejamos 750 ms para observar los símbolos finales.
            reelArea.postDelayed({
                renderResult(result)
                saveResult(result)
            }, 750L)
        }
    }

    private fun saveResult(result: GameResult) {
        disposables.add(
            repository.saveGame(result).subscribe(
                { },
                {
                    Toast.makeText(this, "Error al guardar la partida", Toast.LENGTH_SHORT).show()
                }
            )
        )
        disposables.add(
            repository.saveBalance(result.balanceAfter).subscribe(
                { },
                {
                    Toast.makeText(this, "Error al guardar el saldo", Toast.LENGTH_SHORT).show()
                }
            )
        )
    }

    fun renderResult(result: GameResult) {
        val previousBalance = balance
        balance = result.balanceAfter

        // Verde solo si el saldo final supera al anterior a la tirada.
        val color = when {
            balance > previousBalance -> balancePositiveColor
            balance < previousBalance -> balanceNegativeColor
            else -> balanceNeutralColor
        }
        updateBalanceDisplay(balance, color)

        when (result.outcome) {
            Outcome.JACKPOT -> {
                UiAnimations.jackpotPulse(ivReel1)
                UiAnimations.jackpotPulse(ivReel2)
                UiAnimations.jackpotPulse(ivReel3)
                reelArea.postDelayed({ showResultDialog(result) }, 500L)
            }

            Outcome.WIN -> {
                animateWinningReels(result)
                reelArea.postDelayed({ showResultDialog(result) }, 400L)
            }

            Outcome.LOSS -> {
                UiAnimations.shake(tvBalance)
                if (balance < GameRules.VALID_BETS.min()) {
                    reelArea.postDelayed({ showGameOverDialog(result) }, 350L)
                } else {
                    reelArea.postDelayed({ showResultDialog(result) }, 350L)
                }
            }
        }
    }

    private fun animateWinningReels(result: GameResult) {
        if (result.reel1 == result.reel2) {
            UiAnimations.pulseWin(ivReel1)
            UiAnimations.pulseWin(ivReel2)
        }
        if (result.reel1 == result.reel3) {
            UiAnimations.pulseWin(ivReel1)
            UiAnimations.pulseWin(ivReel3)
        }
        if (result.reel2 == result.reel3) {
            UiAnimations.pulseWin(ivReel2)
            UiAnimations.pulseWin(ivReel3)
        }
    }

    private fun showResultDialog(result: GameResult) {
        val view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_result_p2, null)

        val tvTitle = view.findViewById<TextView>(R.id.tvResultTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvResultMessage)
        val tvDialogBalance = view.findViewById<TextView>(R.id.tvResultBalance)
        val ivResult1 = view.findViewById<ImageView>(R.id.ivResult1)
        val ivResult2 = view.findViewById<ImageView>(R.id.ivResult2)
        val ivResult3 = view.findViewById<ImageView>(R.id.ivResult3)
        val btnPrimary = view.findViewById<Button>(R.id.btnResultPrimary)
        val btnSecondary = view.findViewById<Button>(R.id.btnResultSecondary)

        btnSecondary.visibility = View.GONE
        ivResult1.setImageResource(drawableFor(result.reel1))
        ivResult2.setImageResource(drawableFor(result.reel2))
        ivResult3.setImageResource(drawableFor(result.reel3))
        tvDialogBalance.text = "Saldo: $balance fichas"

        when (result.outcome) {
            Outcome.JACKPOT -> {
                tvTitle.text = "¡JACKPOT!"
                tvMessage.text = "+${result.prize} fichas"
            }
            Outcome.WIN -> {
                val threeOfAKind = listOf(
                    result.reel1,
                    result.reel2,
                    result.reel3
                ).distinct().size == 1
                tvTitle.text = if (threeOfAKind) "¡TRES IGUALES!" else "¡DOS IGUALES!"
                tvMessage.text = "+${result.prize} fichas"
            }
            Outcome.LOSS -> {
                tvTitle.text = "SIN PREMIO"
                tvMessage.text = "Has perdido $selectedBet fichas"
            }
        }

        btnPrimary.text = "VOLVER A APOSTAR"

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        dialog.setCancelable(false)

        // NUEVO: oscurece el fondo un 82% y permite ver solo el diálogo destacado.
        dialog.setOnShowListener {
            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setDimAmount(0.82f)
            }
        }

        btnPrimary.setOnClickListener {
            dialog.dismiss()
            updateBalanceNeutral()
            btnMachine.isEnabled = true
        }

        dialog.show()
    }

    private fun showGameOverDialog(lastResult: GameResult? = null) {
        btnMachine.isEnabled = false
        updateBalanceDisplay(balance, balanceNegativeColor)

        val view = LayoutInflater.from(this)
            .inflate(R.layout.dialog_result_p2, null)

        val tvTitle = view.findViewById<TextView>(R.id.tvResultTitle)
        val tvMessage = view.findViewById<TextView>(R.id.tvResultMessage)
        val tvDialogBalance = view.findViewById<TextView>(R.id.tvResultBalance)
        val ivResult1 = view.findViewById<ImageView>(R.id.ivResult1)
        val ivResult2 = view.findViewById<ImageView>(R.id.ivResult2)
        val ivResult3 = view.findViewById<ImageView>(R.id.ivResult3)
        val btnPrimary = view.findViewById<Button>(R.id.btnResultPrimary)
        val btnSecondary = view.findViewById<Button>(R.id.btnResultSecondary)

        tvTitle.text = "FIN DEL JUEGO"
        tvMessage.text = "No tienes fichas suficientes para seguir apostando."
        tvDialogBalance.text = "Saldo: $balance fichas"

        if (lastResult != null) {
            ivResult1.setImageResource(drawableFor(lastResult.reel1))
            ivResult2.setImageResource(drawableFor(lastResult.reel2))
            ivResult3.setImageResource(drawableFor(lastResult.reel3))
        } else {
            ivResult1.visibility = View.GONE
            ivResult2.visibility = View.GONE
            ivResult3.visibility = View.GONE
        }

        btnPrimary.text = "REINICIAR"
        btnSecondary.text = "SALIR"
        btnSecondary.visibility = View.VISIBLE

        val dialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        dialog.setCancelable(false)

        // NUEVO: el fondo se oscurece también al terminar la partida.
        dialog.setOnShowListener {
            dialog.window?.apply {
                setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND)
                setDimAmount(0.82f)
            }
        }

        btnPrimary.setOnClickListener {
            balance = GameRules.INITIAL_BALANCE
            updateBalanceNeutral()

            disposables.add(
                repository.resetBalance().subscribe({ }, { })
            )
            disposables.add(
                repository.startNewGame().subscribe({ }, { })
            )

            dialog.dismiss()
            btnMachine.isEnabled = true
        }

        btnSecondary.setOnClickListener {
            dialog.dismiss()
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            startActivity(intent)
            finish()
        }

        dialog.show()
    }

    private fun updateBalanceNeutral() {
        updateBalanceDisplay(balance, balanceNeutralColor)
    }

    private fun updateBalanceDisplay(amount: Int, color: Int) {
        tvBalance.text = "Saldo: $amount"
        tvBalance.setTextColor(color)
    }

    private fun drawableFor(symbol: Symbol): Int = when (symbol) {
        Symbol.GAS_MASK -> R.drawable.gas_mask
        Symbol.RADIOACTIVE -> R.drawable.radioactive
        Symbol.BURNING_EARTH -> R.drawable.burning_earth
        Symbol.NUCLEAR_EXPLOSION -> R.drawable.nuclear_explosion
        Symbol.APK_CRATE -> R.drawable.apk_crate
        Symbol.SKULL -> R.drawable.skull
    }

    override fun onDestroy() {
        super.onDestroy()
        reelAnimator.reset()
        disposables.clear()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

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
