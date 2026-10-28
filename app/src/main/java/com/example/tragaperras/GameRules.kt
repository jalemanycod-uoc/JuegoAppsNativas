package com.example.tragaperras

// Reglas del juego. Un "object" es una clase de la que solo existe una copia, y se usa directamente: GameRules.INITIAL_BALANCE

object GameRules {

    const val INITIAL_BALANCE = 250      // monedas al empezar
    val VALID_BETS = listOf(10, 25, 50)  // apuestas posibles
    const val REEL_COUNT = 3             // número de rodillos
    const val SYMBOL_COUNT = 6           // número de símbolos distintos

    val JACKPOT_SYMBOL = Symbol.APK_CRATE  // el jackpot solo sale con 3 de este símbolo

    const val MULTIPLIER_TWO = 2         // 2 iguales: apuesta x2
    const val MULTIPLIER_THREE = 3       // 3 iguales (que no sean el jackpot): apuesta x3
    const val MULTIPLIER_JACKPOT = 15    // 3 símbolos del jackpot: apuesta x15

    // ¿Es jackpot? Los 3 son iguales y son el símbolo del jackpot
    fun isJackpot(symbols: List<Symbol>): Boolean {
        return symbols.distinct().size == 1 && symbols[0] == JACKPOT_SYMBOL
    }

    // Calcula cuántas monedas se ganan con estos símbolos y esta apuesta
    fun payoutFor(symbols: List<Symbol>, bet: Int): Int {
        // distinct() quita los repetidos. Si quedan:
        // 1 símbolo  -> los 3 eran iguales
        // 2 símbolos -> había 2 iguales
        // 3 símbolos -> ninguno igual
        val differentSymbols = symbols.distinct().size

        return when {
            isJackpot(symbols) -> bet * MULTIPLIER_JACKPOT
            differentSymbols == 1 -> bet * MULTIPLIER_THREE
            differentSymbols == 2 -> bet * MULTIPLIER_TWO
            else -> 0
        }
    }
}