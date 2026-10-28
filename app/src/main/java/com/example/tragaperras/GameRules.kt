package com.example.tragaperras

// Reglas del juego. Un "object" es una clase de la que solo existe una copia,
// y se usa directamente: GameRules.INITIAL_BALANCE
object GameRules {

    const val INITIAL_BALANCE = 250      // monedas al empezar
    val VALID_BETS = listOf(10, 25, 50)  // apuestas posibles
    const val REEL_COUNT = 3             // número de rodillos
    const val SYMBOL_COUNT = 6           // número de símbolos distintos

    const val MULTIPLIER_TWO = 1         // 2 símbolos iguales: apuesta x2
    const val MULTIPLIER_THREE = 5      // 3 símbolos iguales: apuesta x10

    // Calcula cuántas monedas se ganan con estos símbolos y esta apuesta
    fun payoutFor(symbols: List<Symbol>, bet: Int): Int {
        // distinct() quita los repetidos. Si quedan:
        // 1 símbolo  -> los 3 eran iguales
        // 2 símbolos -> había 2 iguales
        // 3 símbolos -> ninguno igual
        val differentSymbols = symbols.distinct().size

        return when (differentSymbols) {
            1 -> bet * MULTIPLIER_THREE
            2 -> bet * MULTIPLIER_TWO
            else -> 0
        }
    }
}