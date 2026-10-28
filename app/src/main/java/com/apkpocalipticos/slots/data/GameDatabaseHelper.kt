package com.apkpocalipticos.slots.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

// Clase ayudante para gestionar la base de datos SQLite local del juego
class GameDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "apocalypse_slots.db"
        private const val DATABASE_VERSION = 1
        private const val TABLE_HISTORY = "game_history"

        // Columnas para guardar el saldo del búnker y la fecha de cada tirada
        private const val COL_ID = "id"
        private const val COL_COINS = "coins"
        private const val COL_TIMESTAMP = "timestamp"
    }

    // Se ejecuta al instalar la app para crear nuestra tabla SQLite
    override fun onCreate(db: SQLiteDatabase?) {
        val createTableQuery = "CREATE TABLE $TABLE_HISTORY (" +
                "$COL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "$COL_COINS INTEGER, " +
                "$COL_TIMESTAMP TEXT)"
        db?.execSQL(createTableQuery)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_HISTORY")
        onCreate(db)
    }

    // --- GUARDAR PARTIDA DE FORMA ASÍNCRONA (RxJava Completable) ---
    // Usamos Completable porque solo insertamos datos y no devolvemos resultados
    fun saveGameResultRx(coins: Int, timestamp: String): Completable {
        return Completable.fromAction {
            val db = writableDatabase
            val values = ContentValues().apply {
                put(COL_COINS, coins)
                put(COL_TIMESTAMP, timestamp)
            }
            db.insert(TABLE_HISTORY, null, values)
            db.close() // Cerramos conexión para evitar fugas de memoria
        }
    }

    // --- LEER MONEDAS DE FORMA ASÍNCRONA (RxJava Single) ---
    // Usamos Single porque devuelve un único valor (el saldo actual)
    fun getLastCoinsRx(): Single<Int> {
        return Single.fromCallable {
            val db = readableDatabase
            val cursor = db.rawQuery("SELECT $COL_COINS FROM $TABLE_HISTORY ORDER BY $COL_ID DESC LIMIT 1", null)

            var coins = 100 // 100 monedas o raciones de supervivencia iniciales por defecto

            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(COL_COINS)
                if (index != -1) {
                    coins = cursor.getInt(index)
                }
            }
            cursor.close()
            db.close()

            coins
        }
    }
}