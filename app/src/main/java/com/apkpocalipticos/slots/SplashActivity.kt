package com.apkpocalipticos.slots

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Vinculamos esta clase lógica con el diseño apocalíptico XML que acabamos de crear
        setContentView(R.layout.activity_splash)

        // Usamos un Handler para esperar 2 segundos (2000 milisegundos) en la pantalla de carga
        Handler(Looper.getMainLooper()).postDelayed({
            // Preparamos el salto hacia la pantalla principal del juego (GameActivity)
            val intent = Intent(this, GameActivity::class.java)
            startActivity(intent)

            // Cerramos esta actividad (Splash) para que al pulsar el botón 'Atrás' del móvil no vuelva aquí
            finish()
        }, 2000)
    }
}