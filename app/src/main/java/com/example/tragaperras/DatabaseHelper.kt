package com.example.tragaperras

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteException

// Crea y gestiona la base de datos SQLite de la app
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "tragaperras.db"
        const val DATABASE_VERSION = 2   // NUEVO: subimos la versión porque cambian las tablas
    }

    // Crea la tablas y se ejecuta solo la primera vez
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE player_state (
                id INTEGER PRIMARY KEY,
                balance INTEGER NOT NULL,
                game_number INTEGER NOT NULL
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE game_results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                game_number INTEGER NOT NULL,
                player_name TEXT,
                timestamp INTEGER NOT NULL,
                bet INTEGER NOT NULL,
                reel1 TEXT NOT NULL,
                reel2 TEXT NOT NULL,
                reel3 TEXT NOT NULL,
                outcome TEXT NOT NULL,
                prize INTEGER NOT NULL,
                balance_after INTEGER NOT NULL
            )
            """
        )

        // Se crea la única fila del jugador: saldo inicial y partida número 1
        db.execSQL(
            "INSERT INTO player_state (id, balance, game_number) VALUES (1, ${GameRules.INITIAL_BALANCE}, 1)"
        )
    }

    // Se ejecuta si cambiamos DATABASE_VERSION: borra las tablas y las vuelve a crear
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS game_results")
        db.execSQL("DROP TABLE IF EXISTS player_state")
        onCreate(db)
    }

    // Guarda una tirada con el número de la partida actual
    fun insertGame(result: GameResult): Long {
        val values = ContentValues()
        values.put("game_number", readGameNumber())   // NUEVO
        values.put("player_name", result.playerName)
        values.put("timestamp", result.timestamp)
        values.put("bet", result.bet)
        values.put("reel1", result.reel1.name)   // guardamos el nombre del símbolo, por ejemplo "SKULL"
        values.put("reel2", result.reel2.name)
        values.put("reel3", result.reel3.name)
        values.put("outcome", result.outcome.name)
        values.put("prize", result.prize)
        values.put("balance_after", result.balanceAfter)
        return writableDatabase.insert("game_results", null, values)
    }

    /**
     * Guarda una tirada y su saldo final en la MISMA transacción SQLite.
     * Si falla cualquiera de las dos operaciones, ninguna queda aplicada.
     * Mantiene insertGame()/updateBalance() para compatibilidad con el código previo.
     */
    fun saveSpinAtomically(result: GameResult) {
        val db = writableDatabase
        db.beginTransaction()
        try {
            val insertedId = insertGame(result)
            if (insertedId == -1L) {
                throw SQLiteException("No se pudo guardar la tirada")
            }
            val values = ContentValues().apply { put("balance", result.balanceAfter) }
            val updated = db.update("player_state", values, "id = 1", null)
            if (updated != 1) {
                throw SQLiteException("No se pudo actualizar el saldo")
            }
            db.setTransactionSuccessful()
        } finally {
            db.endTransaction()
        }
    }

    // Todas las tiradas, de la más nueva a la más antigua
    fun queryHistory(): List<GameResult> {
        return readGames("SELECT * FROM game_results ORDER BY timestamp DESC LIMIT 250")
    }

    // Las 5 mejores partidas: una tirada por partida y desempate determinista.
    fun queryBestResults(): List<GameResult> {
        return readGames(
            """
            SELECT g.*
            FROM game_results AS g
            WHERE g.id = (
                SELECT g2.id FROM game_results AS g2
                WHERE g2.game_number = g.game_number
                ORDER BY g2.balance_after DESC, g2.timestamp DESC, g2.id DESC
                LIMIT 1
            )
            ORDER BY g.balance_after DESC, g.timestamp DESC, g.id DESC
            LIMIT 5
            """
        )
    }

    // Lee el saldo guardado del jugador
    fun readBalance(): Int {
        var balance = GameRules.INITIAL_BALANCE
        val cursor = readableDatabase.rawQuery("SELECT balance FROM player_state WHERE id = 1", null)
        if (cursor.moveToFirst()) {
            balance = cursor.getInt(0)
        }
        cursor.close()
        return balance
    }

    // Guarda el nuevo saldo del jugador
    fun updateBalance(balance: Int) {
        val values = ContentValues()
        values.put("balance", balance)
        writableDatabase.update("player_state", values, "id = 1", null)
    }

    // Lee el número de la partida actual
    fun readGameNumber(): Int {
        var gameNumber = 1
        val cursor = readableDatabase.rawQuery("SELECT game_number FROM player_state WHERE id = 1", null)
        if (cursor.moveToFirst()) {
            gameNumber = cursor.getInt(0)
        }
        cursor.close()
        return gameNumber
    }

    // Empieza una partida nueva sumando 1 al contador
    fun startNewGame() {
        writableDatabase.execSQL(
            "UPDATE player_state SET game_number = game_number + 1, balance = ${GameRules.INITIAL_BALANCE} WHERE id = 1"
        )
    }

    // Ejecuta una consulta SELECT y convierte cada fila en un GameResult
    private fun readGames(sql: String): List<GameResult> {
        val games = mutableListOf<GameResult>()
        val cursor = readableDatabase.rawQuery(sql, null)

        // El cursor va recorriendo las filas una a una
        while (cursor.moveToNext()) {
            val game = GameResult(
                id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                playerName = cursor.getString(cursor.getColumnIndexOrThrow("player_name")) ?: "",
                timestamp = cursor.getLong(cursor.getColumnIndexOrThrow("timestamp")),
                bet = cursor.getInt(cursor.getColumnIndexOrThrow("bet")),
                reel1 = Symbol.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("reel1"))),
                reel2 = Symbol.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("reel2"))),
                reel3 = Symbol.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("reel3"))),
                outcome = Outcome.valueOf(cursor.getString(cursor.getColumnIndexOrThrow("outcome"))),
                prize = cursor.getInt(cursor.getColumnIndexOrThrow("prize")),
                balanceAfter = cursor.getInt(cursor.getColumnIndexOrThrow("balance_after"))
            )
            games.add(game)
        }
        cursor.close()
        return games
    }
}