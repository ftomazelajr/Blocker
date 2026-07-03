package com.fernando.blocker

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView

class BlockedOverlayActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_blocked)

        findViewById<TextView>(R.id.textMessage).text =
            getString(R.string.blocked_message)

        findViewById<Button>(R.id.btnClose).setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        finish()
    }
}
