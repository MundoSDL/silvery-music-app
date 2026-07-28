package com.silverymusic.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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

    // Playback now lives in PlaybackService so it can keep running while the app is
    // backgrounded; the service releases it (on swipe-away or destroy), not the Activity.
}
