package com.example.tragaperras

import android.content.Context
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

// Intermediario entre las pantallas y la base de datos.
// Usa RxJava para que las consultas a SQLite se hagan en segundo plano.
class GameRepository(context: Context) {

    private val db = DatabaseHelper(context)

    // Guarda una partida
    fun saveGame(result: GameResult): Completable {
        return Completable.fromAction { db.insertGame(result) }
            .subscribeOn(Schedulers.io())               // se ejecuta en segundo plano
            .observeOn(AndroidSchedulers.mainThread())  // el aviso de "terminado" llega a la pantalla
    }

    // Devuelve todas las partidas
    fun getHistory(): Single<List<GameResult>> {
        return Single.fromCallable { db.queryHistory() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    // Devuelve las 5 mejores partidas
    fun getBestResults(): Single<List<GameResult>> {
        return Single.fromCallable { db.queryBestResults() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    // Devuelve el saldo guardado
    fun getBalance(): Single<Int> {
        return Single.fromCallable { db.readBalance() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    // Guarda el saldo
    fun saveBalance(balance: Int): Completable {
        return Completable.fromAction { db.updateBalance(balance) }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }

    // Vuelve a poner el saldo inicial (cuando se reinicia la partida)
    fun resetBalance(): Completable {
        return saveBalance(GameRules.INITIAL_BALANCE)
    }

    // Empieza una partida nueva (suma 1 al contador de partidas)
    fun startNewGame(): Completable {
        return Completable.fromAction { db.startNewGame() }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}