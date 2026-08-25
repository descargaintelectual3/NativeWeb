package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.data.local.AppDatabase
import com.example.data.model.WebAppEntity
import com.example.ui.screens.InAppBrowserScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.TurboPowerManager
import com.example.util.WebShortcutHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class StandaloneAppActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        // Make truly fullscreen immersive
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val insetsController = WindowCompat.getInsetsController(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        insetsController.hide(WindowInsetsCompat.Type.systemBars())

        val appId = intent.getLongExtra(WebShortcutHelper.EXTRA_WEB_APP_ID, -1L)
        val intentUrl = intent.getStringExtra(WebShortcutHelper.EXTRA_WEB_URL) ?: "https://google.com"
        val intentName = intent.getStringExtra(WebShortcutHelper.EXTRA_WEB_NAME) ?: "WebNative App"

        // Keep screen awake for maximum immersion
        TurboPowerManager.setKeepScreenOn(this, true)

        setContent {
            MyApplicationTheme(darkTheme = true) {
                var loadedApp by remember { mutableStateOf<WebAppEntity?>(null) }

                LaunchedEffect(appId) {
                    if (appId > 0) {
                        withContext(Dispatchers.IO) {
                            val db = AppDatabase.getDatabase(applicationContext, CoroutineScope(Dispatchers.IO))
                            val app = db.webAppDao().getWebAppById(appId)
                            if (app != null) {
                                db.webAppDao().recordAppOpen(appId)
                                withContext(Dispatchers.Main) {
                                    loadedApp = app
                                }
                            }
                        }
                    } else {
                        loadedApp = WebAppEntity(
                            name = intentName,
                            url = intentUrl,
                            isFullscreen = true,
                            isHardwareBoostEnabled = true,
                            isAdBlockEnabled = true
                        )
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Black
                ) {
                    InAppBrowserScreen(
                        app = loadedApp,
                        instantUrl = intentUrl,
                        onClose = { finish() }
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        TurboPowerManager.setKeepScreenOn(this, false)
        TurboPowerManager.releaseWakeLock()
    }
}
