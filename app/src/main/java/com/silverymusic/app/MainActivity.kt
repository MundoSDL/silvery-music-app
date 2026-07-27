package com.silverymusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.silverymusic.app.data.AppContainer
import com.silverymusic.app.navigation.SilveryApp
import com.silverymusic.app.theme.SilveryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SilveryTheme {
                SilveryApp()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // isFinishing guards against a rotation tearing down the player mid-track.
        if (isFinishing) AppContainer.releasePlayback()
    }
}
