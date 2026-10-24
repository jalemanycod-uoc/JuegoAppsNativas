package com.apkpocalipticos.slots.model

import com.apkpocalipticos.slots.game.Outcome
import com.apkpocalipticos.slots.game.Symbol

data class GameResult(

    val reel1: Symbol,
    val reel2: Symbol,
    val reel3: Symbol,

    val bet: Int,

    val outcome: Outcome,

    val prize: Int,

    val balanceAfter: Int

)