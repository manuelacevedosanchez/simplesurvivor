package es.masmultimedia.android

import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.backends.android.AndroidApplication
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration
import es.masmultimedia.game.SimpleSurvivorGame

class AndroidLauncher : AndroidApplication() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configure UncaughtExceptionHandler
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Gdx.app.error(
                "UncaughtException",
                "Unhandled exception in thread ${thread.name}",
                throwable
            )
            // Optional: Show a message to the user or perform additional action
        }

        val config = AndroidApplicationConfiguration().apply {
            // hideStatusBar and useImmersiveMode are deprecated, we'll handle immersive mode manually
        }
        initialize(SimpleSurvivorGame(), config)
        enterImmersiveMode()
    }

    @Suppress("DEPRECATION")
    private fun enterImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API level 30 and above
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // API level 29 and below
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        }
    }

    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        // Don't call super.onBackPressed() to prevent the activity from closing the app
        Gdx.app.postRunnable {
            Gdx.input.inputProcessor?.keyDown(Input.Keys.BACK)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enterImmersiveMode()
        }
    }
}
