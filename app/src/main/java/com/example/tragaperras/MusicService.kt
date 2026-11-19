package com.example.tragaperras

import android.app.Service
import android.content.Intent
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.IBinder

// Servicio que se encarga de la música de fondo del juego.
// Un servicio es un componente de Android sin pantalla, que sigue funcionando por detrás mientras el jugador cambia de pantalla.
class MusicService : Service() {

    // Valores que comparten el servicio y SoundManager.
    // Al estar en "companion object", se pueden usar desde fuera escribiendo MusicService.ACTION_PLAY, sin tener que crear el servicio
    companion object {
        // Órdenes que puede recibir el servicio
        const val ACTION_PLAY = "play"
        const val ACTION_PAUSE = "pause"
        const val ACTION_STOP = "stop"

        // Dato que acompaña a la orden de poner música: qué música es
        const val EXTRA_MUSIC_ID = "music_id"

        // Volumen de la música: 0 = silencio, 1 = máximo
        const val MUSIC_VOLUME = 0.4f
    }

    // Reproductor de la música
    private var musicPlayer: MediaPlayer? = null

    // Qué música está puesta: R.raw.musica_menu, R.raw.musica_partida,
    // -1 si es la canción elegida por el jugador, o 0 si no hay ninguna
    private var currentMusic = 0

    // true mientras la música se está preparando en otro hilo
    private var isPreparing = false

    // true si la música tiene que sonar y false si la hemos pausado
    private var shouldPlay = false

    // true si la música se ha pausado por una llamada, una alarma u otra música
    private var pausedBySystem = false

    // Servicio de Android que reparte el turno de sonido entre las apps
    private lateinit var audioManager: AudioManager

    // Se ejecuta una sola vez, cuando se crea el servicio
    override fun onCreate() {
        super.onCreate()
        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
    }

    // Se ejecuta cada vez que SoundManager le envía una orden
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent != null) {
            val action = intent.action
            if (action == ACTION_PLAY) {
                val musicId = intent.getIntExtra(EXTRA_MUSIC_ID, R.raw.musica_menu)
                playMusic(musicId)
            } else if (action == ACTION_PAUSE) {
                pauseMusic()
            } else if (action == ACTION_STOP) {
                stopMusic()
            }
        }
        // Si Android cierra el servicio por falta de memoria, no hace falta que lo vuelva a abrir solo. La próxima pantalla que se abra le enviará otra orden
        return START_NOT_STICKY
    }

    // Este servicio solo recibe órdenes, no se conecta a las pantallas. Por eso devuelve null
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    // Se ejecuta al cerrar el servicio: liberamos la música y devolvemos el turno de sonido
    override fun onDestroy() {
        stopMusic()
        audioManager.abandonAudioFocus(focusListener)
        super.onDestroy()
    }

    // Llamadas, alarmas y otras músicas

    // Android nos avisa aquí cuando perdemos o recuperamos el turno de sonido
    private val focusListener = AudioManager.OnAudioFocusChangeListener { focusChange ->
        if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
            // Ha terminado la llamada, la alarma o la otra música, si la hemos pausado por eso, vuelve a sonar
            if (pausedBySystem) {
                pausedBySystem = false
                shouldPlay = true
                val player = musicPlayer
                if (player != null && !isPreparing) {
                    player.start()
                }
            }
        } else {
            // Llamada, alarma u otra música, pausamos la nuestra
            if (shouldPlay) {
                shouldPlay = false
                pausedBySystem = true
                val player = musicPlayer
                if (player != null && player.isPlaying) {
                    player.pause()
                }
            }
        }
    }

    // Pide a Android el turno para poner música.
    // Devuelve false si ahora no se puede (por ejemplo, durante una llamada)
    private fun requestAudioFocus(): Boolean {
        val result = audioManager.requestAudioFocus(
            focusListener,
            AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN
        )
        return result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
    }

    // Música de fondo

    // Pone a sonar una música en bucle
    private fun playMusic(musicId: Int) {
        // Si ahora mismo hay una llamada, Android nos deniega el turno y no ponemos música
        if (!requestAudioFocus()) {
            return
        }
        shouldPlay = true
        pausedBySystem = false

        // Si el jugador ha elegido una canción del móvil, suena esa en lugar de la oficial.
        // La marco con el número -1
        val customMusic = SoundManager.getCustomMusic(this)
        var musicToPlay = musicId
        if (customMusic != null) {
            musicToPlay = -1
        }

        // Si ya está puesta esta misma música, solo se reanuda
        // Si todavía se está preparando, empezará sola cuando termine
        val player = musicPlayer
        if (player != null && currentMusic == musicToPlay) {
            if (!isPreparing && !player.isPlaying) {
                player.start()
            }
            return
        }

        // Si era otra música, se quita y se prepara la nueva
        stopMusic()
        shouldPlay = true
        val newPlayer = MediaPlayer()

        // Le dice al reproductor qué archivo tiene que tocar
        try {
            if (customMusic != null) {
                newPlayer.setDataSource(this, Uri.parse(customMusic))
            } else {
                // Los archivos de la carpeta raw se abren así
                val file = resources.openRawResourceFd(musicId)
                newPlayer.setDataSource(file.fileDescriptor, file.startOffset, file.length)
                file.close()
            }
        } catch (e: Exception) {
            // No se ha podido abrir. Si era la canción elegida (por ejemplo, se ha borrado del móvil), vuelve a la música oficial
            newPlayer.release()
            if (customMusic != null) {
                SoundManager.setCustomMusic(this, null)
                playMusic(musicId)
            }
            return
        }

        newPlayer.isLooping = true                          // que se repita sin parar
        newPlayer.setVolume(MUSIC_VOLUME, MUSIC_VOLUME)     // altavoz izquierdo y derecho

        // Cuando la música termine de prepararse, empezará a sonar, si no está pausada
        newPlayer.setOnPreparedListener { preparedPlayer ->
            isPreparing = false
            if (shouldPlay) {
                preparedPlayer.start()
            }
        }

        // Si hay un error al preparar la música, se quita
        // Si era la canción elegida, vuelve a la oficial
        newPlayer.setOnErrorListener { _, _, _ ->
            stopMusic()
            if (customMusic != null) {
                SoundManager.setCustomMusic(this, null)
                playMusic(musicId)
            }
            true   // true = "el error ya está gestionado"
        }

        musicPlayer = newPlayer
        currentMusic = musicToPlay
        isPreparing = true

        // Prepara la música en otro hilo, que gestiona el propio MediaPlayer. 
        newPlayer.prepareAsync()
    }

    // Pausa la música
    private fun pauseMusic() {
        shouldPlay = false
        pausedBySystem = false
        val player = musicPlayer
        if (player != null && player.isPlaying) {
            player.pause()
        }
    }

    // Para la música del todo y libera la memoria que usaba
    private fun stopMusic() {
        val player = musicPlayer
        if (player != null) {
            player.release()
        }
        musicPlayer = null
        currentMusic = 0
        isPreparing = false
        shouldPlay = false
        pausedBySystem = false
    }
}