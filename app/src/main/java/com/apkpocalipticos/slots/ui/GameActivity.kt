package com.apkpocalipticos.slots.ui

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apkpocalipticos.slots.R
import com.apkpocalipticos.slots.game.GameRules
import com.apkpocalipticos.slots.game.Outcome
import com.apkpocalipticos.slots.game.SlotMachineEngine

class GameActivity : AppCompatActivity() {

    private val engine = SlotMachineEngine()

    private var currentBalance = GameRules.INITIAL_BALANCE
    private var currentBet = 25

    private lateinit var tvPlayer: TextView
    private lateinit var tvBalance: TextView
    private lateinit var tvBet: TextView

    private lateinit var tvReel1: TextView
    private lateinit var tvReel2: TextView
    private lateinit var tvReel3: TextView

    private lateinit var tvResult: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        tvPlayer = findViewById(R.id.tvPlayer)
        tvBalance = findViewById(R.id.tvBalance)
        tvBet = findViewById(R.id.tvBet)

        tvReel1 = findViewById(R.id.tvReel1)
        tvReel2 = findViewById(R.id.tvReel2)
        tvReel3 = findViewById(R.id.tvReel3)

        tvResult = findViewById(R.id.tvResult)

        val btnBet10 = findViewById<Button>(R.id.btnBet10)
        val btnBet25 = findViewById<Button>(R.id.btnBet25)
        val btnBet50 = findViewById<Button>(R.id.btnBet50)
        val btnSpin = findViewById<Button>(R.id.btnSpin)

        val playerName = intent.getStringExtra("PLAYER_NAME") ?: "Jugador"

        tvPlayer.text = playerName

        updateInterface()

        btnBet10.setOnClickListener {
            selectBet(10)
        }

        btnBet25.setOnClickListener {
            selectBet(25)
        }

        btnBet50.setOnClickListener {
            selectBet(50)
        }

        btnSpin.setOnClickListener {
            spin()
        }
    }

    private fun selectBet(bet: Int) {

        if (bet > currentBalance) {

            Toast.makeText(
                this,
                "No tienes saldo suficiente",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        currentBet = bet
        tvBet.text = "Apuesta: $currentBet"
    }

    private fun spin() {

        if (currentBalance < currentBet) {

            Toast.makeText(
                this,
                "Saldo insuficiente",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        val result = engine.spin(
            bet = currentBet,
            currentBalance = currentBalance
        )

        currentBalance = result.balanceAfter

        tvReel1.text = result.reel1.name
        tvReel2.text = result.reel2.name
        tvReel3.text = result.reel3.name

        tvResult.text = when (result.outcome) {

            Outcome.WIN ->
                "¡VICTORIA! +${result.prize}"

            Outcome.LOSS ->
                if (currentBalance < 10) {
                    "FIN DEL JUEGO"
                } else {
                    "¡HAS PERDIDO!"
                }

            Outcome.JACKPOT ->
                "¡JACKPOT! +${result.prize}"
        }

        updateInterface()
    }

    private fun updateInterface() {

        tvBalance.text = "Saldo: $currentBalance"
        tvBet.text = "Apuesta: $currentBet"
    }
}