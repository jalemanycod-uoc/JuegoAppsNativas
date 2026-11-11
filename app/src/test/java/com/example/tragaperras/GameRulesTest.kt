package com.example.tragaperras

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class GameRulesTest {

    @Test fun jackpotThreeApkCrates() {
        val reels = listOf(Symbol.APK_CRATE, Symbol.APK_CRATE, Symbol.APK_CRATE)
        assertTrue(GameRules.isJackpot(reels))
        assertEquals(150, GameRules.payoutFor(reels, 10))
    }

    @Test fun threeIdenticalNonJackpotPayTriple() {
        val reels = listOf(Symbol.SKULL, Symbol.SKULL, Symbol.SKULL)
        assertFalse(GameRules.isJackpot(reels))
        assertEquals(75, GameRules.payoutFor(reels, 25))
    }

    @Test fun twoIdenticalPayDouble() {
        val reels = listOf(Symbol.GAS_MASK, Symbol.GAS_MASK, Symbol.SKULL)
        assertEquals(100, GameRules.payoutFor(reels, 50))
    }

    @Test fun allDifferentPayZero() {
        val reels = listOf(Symbol.GAS_MASK, Symbol.SKULL, Symbol.RADIOACTIVE)
        assertEquals(0, GameRules.payoutFor(reels, 25))
    }
}
