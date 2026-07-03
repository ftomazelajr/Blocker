package com.fernando.blocker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SetupPinActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_setup_pin)

        val pin1 = findViewById<EditText>(R.id.editPin1)
        val pin2 = findViewById<EditText>(R.id.editPin2)
        val btnSave = findViewById<Button>(R.id.btnSavePin)

        btnSave.setOnClickListener {
            val a = pin1.text.toString()
            val b = pin2.text.toString()

            if (a.length < 4) {
                Toast.makeText(this, R.string.pin_too_short, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (a != b) {
                Toast.makeText(this, R.string.pin_mismatch, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            PrefsManager.setPin(this, a)
            Toast.makeText(this, R.string.pin_saved, Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
