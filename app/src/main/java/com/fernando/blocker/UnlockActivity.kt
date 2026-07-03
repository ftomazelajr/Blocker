package com.fernando.blocker

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.concurrent.TimeUnit

class UnlockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlock)

        val pinInput = findViewById<EditText>(R.id.editPin)
        val btnConfirm = findViewById<Button>(R.id.btnConfirmPin)
        val textInfo = findViewById<TextView>(R.id.textUnlockInfo)

        if (PrefsManager.hasUnlockRequestPending(this)) {
            val remaining = PrefsManager.getUnlockRequestRemainingMs(this)
            val hours = TimeUnit.MILLISECONDS.toHours(remaining)
            val minutes = TimeUnit.MILLISECONDS.toMinutes(remaining) % 60
            textInfo.text = getString(R.string.unlock_pending_format, hours, minutes)
            pinInput.isEnabled = false
            btnConfirm.isEnabled = false
            return
        }

        textInfo.text = getString(R.string.unlock_info)

        btnConfirm.setOnClickListener {
            val pin = pinInput.text.toString()
            if (!PrefsManager.verifyPin(this, pin)) {
                Toast.makeText(this, R.string.unlock_wrong_pin, Toast.LENGTH_SHORT).show()
                pinInput.text.clear()
                return@setOnClickListener
            }

            when {
                PrefsManager.isUnlockRequestReady(this) -> {
                    PrefsManager.unlockTemporarily(this)
                    PrefsManager.clearUnlockRequest(this)
                    Toast.makeText(this, R.string.unlock_success, Toast.LENGTH_SHORT).show()
                    finish()
                }
                else -> {
                    PrefsManager.requestUnlock(this)
                    Toast.makeText(this, R.string.unlock_request_created, Toast.LENGTH_LONG).show()
                    finish()
                }
            }
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
