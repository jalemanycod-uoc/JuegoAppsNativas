package com.example.tragaperras

// Datos de una tirada. Una data class sirve solo para guardar datos
data class GameResult(
    val id: Long = 0,          // número de la partida en la base de datos (más adelante)
    val timestamp: Long,       // fecha y hora de la tirada, en milisegundos
    val bet: Int,              // apuesta
    val reel1: Symbol,         // símbolo del rodillo 1
    val reel2: Symbol,         // símbolo del rodillo 2
    val reel3: Symbol,         // símbolo del rodillo 3
    val outcome: Outcome,      // resultado: WIN, LOSS o JACKPOT
    val prize: Int,            // monedas ganadas (0 si se pierde)
    val balanceAfter: Int      // saldo después de la tirada
)