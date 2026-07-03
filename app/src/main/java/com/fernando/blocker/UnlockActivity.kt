package com.fernando.blocker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class UnlockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlock)

        val pinInput = findViewById<EditText>(R.id.editPin)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmPin)
        val textInfo = findViewById<TextView>(R.id.textUnlockInfo)

        textInfo.text = getString(R.string.unlock_info)

        btnConfirm.setOnClickListener {
            val pin = pinInput.text.toString()
            if (PrefsManager.verifyPin(this, pin)) {
                PrefsManager.unlockTemporarily(this)
                Toast.makeText(this, R.string.unlock_success, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, R.string.unlock_wrong_pin, Toast.LENGTH_SHORT).show()
                pinInput.text.clear()
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
