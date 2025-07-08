package com.crzx1337.ilovemusicm3

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.crzx1337.ilovemusicm3.ui.theme.ILoveMusicM3Theme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ILoveMusicM3Theme {
                val viewModel: RadioViewModel = viewModel()
                val context = LocalContext.current
                val currentStation by viewModel.currentStation.collectAsState()
                
                // Bind to service when the composable is created
                DisposableEffect(Unit) {
                    viewModel.bindService(context)
                    onDispose {
                        viewModel.unbindService(context)
                    }
                }
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    // Show start menu if no station is selected, otherwise show the media player
                    if (currentStation == null) {
                        StartMenuScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    } else {
                        MediaPlayerScreen(
                            viewModel = viewModel,
                            modifier = Modifier.padding(innerPadding)
                        )
                    }
                }
            }
        }
    }
}