package com.example.tragaperras

import android.content.Context
import android.media.MediaPlayer
import android.media.AudioManager
import android.net.Uri

// Se encarga de toda la música y los efectos de sonido del juego.

object SoundManager {

    // Reproductor de la música de fondo
    private var musicPlayer: MediaPlayer? = null

    // Qué música está sonando ahora (por ejemplo, R.raw.musica_menu - 0 = ninguna)
    private var currentMusic = 0

    // Reproductor del sonido de giro. Lo guardo para poder pararlo. Sino no deja.
    private var spinPlayer: MediaPlayer? = null

    // Volumen de la música de fondo: 0 = silencio, 1 = máximo.
    // Lo dejo más bajo que los efectos para que no tape los sonidos del juego
    private const val MUSIC_VOLUME = 0.4f

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

    // Para la música y la vuelve a poner (al cambiar de canción)
    fun restartMusic(context: Context, musicId: Int) {
        stopMusic()
        playMusic(context, musicId)
    }

    // Llamadas y alarmas

    // Servicio de Android que reparte el turno de sonido entre las apps
    private var audioManager: AudioManager? = null

    // true si la música se ha pausado por una llamada o una alarma
    private var pausedBySystem = false

    // Android avisa aquí cuando se pierde o se recupera el turno de sonido
    private val focusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ||
            focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            // Llamada, alarma o aviso: pausa la música un momento
            val player = musicPlayer
            if (player != null && player.isPlaying) {
                player.pause()
                pausedBySystem = true
            }
        } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // La llamada o la alarma han terminado: si la música la habíamos pausado por eso, se vuyelve a poner
            val player = musicPlayer
            if (player != null && pausedBySystem) {
                player.start()
            }
            pausedBySystem = false
        } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
            // Otra app ha empezado a poner música: pausa y no vuelve la música
            pauseMusic()
        }
    }

    // Pide a Android el turno para poner música.
    // Devuelve false si ahora no se puede (por ejemplo, durante una llamada)
    private fun requestAudioFocus(context: Context): Boolean {
        var manager = audioManager
        if (manager == null) {
            manager = context.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioManager = manager
        }
        val result = manager.requestAudioFocus(
            focusListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    // Música de fondo

    // Pone a sonar una música en bucle
    fun playMusic(context: Context, musicId: Int) {
        // Si la música está desactivada en Opciones, no suena nada
        if (!isMusicOn(context)) {
            pauseMusic()
            return
        }

        // Se pide a android el turno de sonido
        // Si hubiese una llamada en el momento, se deniega y no se pone la música
        if (!requestAudioFocus(context)) {
            return
        }

        // Si el jugador ha elegido una canción del móvil, suena esa en lugar de la oficial.
        // La marco con el número -1 para saber que es la canción elegida
        val customMusic = getCustomMusic(context)
        var musicToPlay = musicId
        if (customMusic != null) {
            musicToPlay = -1
        }

        // Si ya está puesta esta misma música, solo se reanuda si estaba en pausa
        val player = musicPlayer
        if (player != null && currentMusic == musicToPlay) {
            if (!player.isPlaying) {
                player.start()
            }
            return
        }

        // Si era otra música, se para y se pone la nueva
        stopMusic()
        var newPlayer: MediaPlayer?
        if (customMusic != null) {
            newPlayer = MediaPlayer.create(context.applicationContext, Uri.parse(customMusic))
        } else {
            newPlayer = MediaPlayer.create(context.applicationContext, musicId)
        }

        // Si la canción elegida ya no se puede abrir (por ejemplo, se ha borrado del móvil),
        // se vuelve a la música oficial
        if (newPlayer == null && customMusic != null) {
            setCustomMusic(context, null)
            newPlayer = MediaPlayer.create(context.applicationContext, musicId)
            musicToPlay = musicId
        }

        if (newPlayer != null) {
            newPlayer.isLooping = true   // que se repita sin parar
            newPlayer.setVolume(MUSIC_VOLUME, MUSIC_VOLUME)   // altavoz izquierdo y derecho
            newPlayer.start()
            musicPlayer = newPlayer
            currentMusic = musicToPlay
        }
    }

    // Pausa la música (por ejemplo cuando salimoss de la app)
    fun pauseMusic() {
        val player = musicPlayer
        if (player != null && player.isPlaying) {
            player.pause()
        }
        pausedBySystem = false
    }

    // Para la música del todo y libera la memoria que usaba (importante)
    fun stopMusic() {
        val player = musicPlayer
        if (player != null) {
            player.release()
        }
        musicPlayer = null
        currentMusic = 0
        pausedBySystem = false
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