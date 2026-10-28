package com.example.tragaperras

// La máquina tragaperras: hace la tirada y calcula el resultado
class SlotMachineEngine {

    // Lista con los 6 símbolos posibles
    private val symbols: List<Symbol> = Symbol.values().toList()

    // Hace una tirada con la apuesta y el saldo actuales
    fun spin(bet: Int, balance: Int): GameResult {
        // Elegimos un símbolo al azar para cada rodillo
        val result = listOf(symbols.random(), symbols.random(), symbols.random())

        val prize = calculatePrize(result, bet)
        val outcome = determineOutcome(result)

        // Nuevo saldo: restamos la apuesta y sumamos el premio
        val newBalance = balance - bet + prize

        return GameResult(
            timestamp = System.currentTimeMillis(),
            bet = bet,
            reel1 = result[0],
            reel2 = result[1],
            reel3 = result[2],
            outcome = outcome,
            prize = prize,
            balanceAfter = newBalance
        )
    }

    // El premio lo calcula GameRules
    private fun calculatePrize(symbols: List<Symbol>, bet: Int): Int {
        return GameRules.payoutFor(symbols, bet)
    }

    // Decide si es victoria, derrota o jackpot
    private fun determineOutcome(symbols: List<Symbol>): Outcome {
        return when {
            GameRules.isJackpot(symbols) -> Outcome.JACKPOT   // 3 símbolos del jackpot
            symbols.distinct().size <= 2 -> Outcome.WIN       // 2 o 3 iguales
            else -> Outcome.LOSS                              // ninguno igual
        }
    }
}