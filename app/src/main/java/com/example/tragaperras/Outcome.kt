package com.example.tragaperras

// Los tres resultados posibles de una tirada
enum class Outcome {
    WIN,     // victoria: 2 símbolos iguales
    LOSS,    // derrota: ningún símbolo igual
    JACKPOT  // premio gordo: 3 símbolos iguales
}