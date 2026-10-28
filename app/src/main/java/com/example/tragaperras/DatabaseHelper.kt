package com.example.tragaperras

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

// Crea y gestiona la base de datos SQLite de l'app
class DatabaseHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "tragaperras.db"
        const val DATABASE_VERSION = 1
    }

    // Crea las tablas (se ejecuta solo la primera vez)
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE player_state (
                id INTEGER PRIMARY KEY,
                balance INTEGER NOT NULL
            )
            """
        )

        db.execSQL(
            """
            CREATE TABLE game_results (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
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

        // Creación de la única fila del jugador con el saldo inicial
        db.execSQL("INSERT INTO player_state (id, balance) VALUES (1, ${GameRules.INITIAL_BALANCE})")
    }

    // Se ejecuta si algún día cambiamos DATABASE_VERSION: borra las tablas y las vuelve a crear
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS game_results")
        db.execSQL("DROP TABLE IF EXISTS player_state")
        onCreate(db)
    }

    // Guarda una partida. Devuelve el id que le ha dado la base de datos
    fun insertGame(result: GameResult): Long {
        val values = ContentValues()
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

    // Todas las partidas, de la más nueva a la más antigua
    fun queryHistory(): List<GameResult> {
        return readGames("SELECT * FROM game_results ORDER BY timestamp DESC")
    }

    // Las 5 mejores puntuaciones (las partidas con el saldo final más alto)
    fun queryBestResults(): List<GameResult> {
        return readGames("SELECT * FROM game_results ORDER BY balance_after DESC LIMIT 5")
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