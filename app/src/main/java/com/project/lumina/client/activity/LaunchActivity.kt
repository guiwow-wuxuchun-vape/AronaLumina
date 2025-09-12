package com.project.lumina.client.activity

import kotlinx.coroutines.launch
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.CompositionLocalProvider
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import androidx.lifecycle.lifecycleScope
import androidx.core.view.WindowInsetsControllerCompat
import com.project.lumina.client.router.launch.AnimatedLauncherScreen
import com.project.lumina.client.ui.theme.LuminaClientTheme
import com.project.lumina.client.util.HashCat
import com.project.lumina.client.util.TrackUtil
import com.project.lumina.client.util.UpdateCheck
import kotlinx.coroutines.delay

class LaunchActivity : ComponentActivity() {

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()


        val updateCheck = UpdateCheck()
        updateCheck.initiateHandshake(this)

        val verifier = HashCat.getInstance()
        val isValid = verifier.LintHashInit(this)
        if (isValid) {
           
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, window.decorView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        setContent {
            LuminaClientTheme {
                CompositionLocalProvider(LocalOverscrollConfiguration provides null) {
                    AnimatedLauncherScreen()
                }
            }
        }
        val sharedPreferences = getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        val skipLoading = sharedPreferences.getBoolean("skipLoading", false)
        if (skipLoading) {
           lifecycleScope.launch {
    startActivityWithTransition(this@LaunchActivity, MinecraftCheckActivity::class.java)
}

        }



    }
}


suspend fun startActivityWithTransition(context: android.content.Context, destinationClass: Class<*>) {
    delay(800)
    val intent = Intent(context, destinationClass)
    context.startActivity(intent)
    (context as? ComponentActivity)?.overridePendingTransition(
        android.R.anim.fade_in,
        android.R.anim.fade_out
    )
    (context as? ComponentActivity)?.finish()
}
