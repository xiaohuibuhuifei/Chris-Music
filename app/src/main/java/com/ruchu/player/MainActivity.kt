package com.ruchu.player

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ruchu.player.ui.navigation.RuChuNavHost
import com.ruchu.player.ui.theme.RuChuTheme
import com.ruchu.player.util.PlaybackManager

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RuChuTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    RuChuNavHost()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    override fun onResume() {
        super.onResume()
        val pm = PlaybackManager.getInstance(this)
        pm.reconnectIfNeeded(this)

        if (intent?.getBooleanExtra(ShuffleActivity.EXTRA_SHUFFLE_ALL, false) == true) {
            intent?.removeExtra(ShuffleActivity.EXTRA_SHUFFLE_ALL)
            pm.shuffleAllFromShortcut(this)
        }
    }
}
