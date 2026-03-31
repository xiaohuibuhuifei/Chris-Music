package com.ruchu.player

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity

class ShuffleActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        or Intent.FLAG_ACTIVITY_CLEAR_TOP
            )
            putExtra(EXTRA_SHUFFLE_ALL, true)
        }
        startActivity(intent)
        finish()
    }

    companion object {
        const val EXTRA_SHUFFLE_ALL = "shuffle_all"
    }
}
