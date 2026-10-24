package com.apkpocalipticos.slots.game

import com.apkpocalipticos.slots.model.GameResult

class SlotMachineEngine {

    private val symbols = Symbol.entries

    fun spin(
        bet: Int,
        currentBalance: Int
    ): GameResult {

        require(bet in GameRules.VALID_BETS) {
            "Apuesta no válida"
        }

        require(currentBalance >= bet) {
            "Saldo insuficiente"
        }

        // Primero se descuenta la apuesta
        val balanceAfterBet = currentBalance - bet

        // Generamos 3 símbolos aleatorios
        val reel1 = symbols.random()
        val reel2 = symbols.random()
        val reel3 = symbols.random()

        val resultSymbols = listOf(
            reel1,
            reel2,
            reel3
        )

        val outcome = determineOutcome(resultSymbols)

        val prize = calculatePrize(
            resultSymbols,
            bet
        )

        val finalBalance = balanceAfterBet + prize

        return GameResult(
            reel1 = reel1,
            reel2 = reel2,
            reel3 = reel3,
            bet = bet,
            outcome = outcome,
            prize = prize,
            balanceAfter = finalBalance
        )
    }

    private fun determineOutcome(
        symbols: List<Symbol>
    ): Outcome {

        return when {

            symbols.all {
                it == Symbol.APK_CRATE
            } -> Outcome.JACKPOT

            symbols.distinct().size == 1 ->
                Outcome.WIN

            else ->
                Outcome.LOSS
        }
    }

    private fun calculatePrize(
        symbols: List<Symbol>,
        bet: Int
    ): Int {

        return when {

            symbols.all {
                it == Symbol.APK_CRATE
            } -> bet * 10

            symbols.distinct().size == 1 ->
                bet * 3

            else ->
                0
        }
    }
}