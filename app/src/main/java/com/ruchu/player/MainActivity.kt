package com.ruchu.player

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.ruchu.player.ui.navigation.RuChuNavHost
import com.ruchu.player.ui.theme.RuChuTheme

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
}
