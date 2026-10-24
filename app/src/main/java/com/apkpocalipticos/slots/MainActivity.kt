package com.apkpocalipticos.slots

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.apkpocalipticos.slots.ui.GameActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        val etPlayerName =
            findViewById<EditText>(R.id.etPlayerName)

        val btnPlay =
            findViewById<Button>(R.id.btnPlay)

        btnPlay.setOnClickListener {

            val playerName =
                etPlayerName.text.toString().trim()

            if (playerName.isEmpty()) {

                etPlayerName.error =
                    "Introduce tu nombre"

                return@setOnClickListener
            }

            val intent =
                Intent(this, GameActivity::class.java)

            intent.putExtra(
                "PLAYER_NAME",
                playerName
            )

            startActivity(intent)
        }
    }
}