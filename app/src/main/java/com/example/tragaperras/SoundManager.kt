package com.example.tragaperras

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer

// Se encarga de toda la música y los efectos de sonido del juego.

object SoundManager {

    // Reproductor del sonido de giro. Lo guardo para poder pararlo. Sino no deja.
    private var spinPlayer: MediaPlayer? = null

    // Pantallas de la app que se están viendo
    private var visibleScreens = 0

    // Cuando empeiza a verse lo llama a onStart
    fun screenStarted() {
        visibleScreens = visibleScreens + 1
    }

    // Cuando deja de verse, llama a onStop
    fun screenStopped() {
        visibleScreens = visibleScreens - 1
        // Cuando el jugador sale
        if (visibleScreens == 0) {
            pauseMusic()
        }
    }

    // Ajustes: se guardan con SharedPreferences, así no se pierden al cerrar la app

    private const val PREFS_NAME = "ajustes"
    private const val KEY_MUSIC_ON = "musica_activada"
    private const val KEY_EFFECTS_ON = "efectos_activados"

    // Mira si está activada la música. Si nunca se ha cambiado, devuelve true
    fun isMusicOn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_MUSIC_ON, true)
    }

    // Guarda si la música está activada o no
    fun setMusicOn(context: Context, musicOn: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_MUSIC_ON, musicOn).apply()
    }

    // Mira si están activados los efectos de sonido. Si nunca se ha cambiado, devuelve true
    fun isEffectsOn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_EFFECTS_ON, true)
    }

    // Guarda si los efectos de sonido están activados o no
    fun setEffectsOn(context: Context, effectsOn: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_EFFECTS_ON, effectsOn).apply()
    }

    // Clave que guarda la canción elegida por el jugador
    private const val KEY_CUSTOM_MUSIC = "musica_elegida"

    // Devuelve la dirección de la canción elegida, o null si se usa la música oficial
    fun getCustomMusic(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_CUSTOM_MUSIC, null)
    }

    // Guarda la canción elegida. Si recibe null, se vuelve a la música oficial
    fun setCustomMusic(context: Context, musicAddress: String?) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_CUSTOM_MUSIC, musicAddress).apply()
    }

    // Música de fondo
    // La música la reproduce MusicService. Aquí solo se le envian órdenes

    // Lo guarda para poder enviar órdenes al servicio desde las funciones que no lo reciben (pauseMusic y stopMusic)
    private var appContext: Context? = null

    // Envía una orden al servicio de música
    private fun sendToMusicService(context: Context, action: String, musicId: Int) {
        val intent = Intent(context, MusicService::class.java)
        intent.action = action
        intent.putExtra(MusicService.EXTRA_MUSIC_ID, musicId)
        context.startService(intent)
    }

    // Pone a sonar una música en bucle
    fun playMusic(context: Context, musicId: Int) {
        appContext = context.applicationContext

        // Si la música está desactivada en Opciones, no suena nada
        if (!isMusicOn(context)) {
            pauseMusic()
            return
        }
        sendToMusicService(context.applicationContext, MusicService.ACTION_PLAY, musicId)
    }

    // Pausa la música cuaando por ejemplo se sale de la app)
    fun pauseMusic() {
        val context = appContext
        if (context != null) {
            sendToMusicService(context, MusicService.ACTION_PAUSE, 0)
        }
    }

    // Para la música del todo el servicio libera la memoria que usaba
    fun stopMusic() {
        val context = appContext
        if (context != null) {
            sendToMusicService(context, MusicService.ACTION_STOP, 0)
        }
    }

    // Para la música y la vuelve a poner, al cambiar de canción
    fun restartMusic(context: Context, musicId: Int) {
        appContext = context.applicationContext
        stopMusic()
        playMusic(context, musicId)
    }

    // LOs efectos de sonido

    // Toca un efecto corto una sola vez (clic, monedas, perder, jackpot, etc.)
    fun playEffect(context: Context, effectId: Int) {
        if (!isEffectsOn(context)) {
            return
        }
        val player = MediaPlayer.create(context.applicationContext, effectId)
        if (player != null) {
            // Cuando acaba el sonido, libera la memoria del reproductor (importante)
            player.setOnCompletionListener { finishedPlayer ->
                finishedPlayer.release()
            }
            player.start()
        }
    }

    // Empieza el sonido de los rodillos girando
    fun playSpin(context: Context) {
        if (!isEffectsOn(context)) {
            return
        }
        stopSpin()
        val player = MediaPlayer.create(context.applicationContext, R.raw.giro)
        if (player != null) {
            player.start()
            spinPlayer = player
        }
    }

    // Para el sonido de los rodillos (cuando se detienen los rodilllos)
    fun stopSpin() {
        val player = spinPlayer
        if (player != null) {
            player.release()
        }
        spinPlayer = null
    }
}