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
        val config = AndroidApplicationConfiguration().apply {
            // hideStatusBar = true // Eliminar o comentar esta línea
            // useImmersiveMode = true // Opcionalmente, comentar esta línea también
        }
        initialize(SimpleSurvivorGame(), config)
        enterImmersiveMode()
    }

    @Suppress("DEPRECATION")
    private fun enterImmersiveMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // API nivel 30 y superiores
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior =
                    WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            // API nivel 29 y anteriores
            window.decorView.systemUiVisibility = (
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                )
        }
    }

    @Deprecated("Deprecated in Java", ReplaceWith(
        "Gdx.app.postRunnable { Gdx.input.inputProcessor.keyDown(Input.Keys.BACK) }",
        "com.badlogic.gdx.Gdx",
        "com.badlogic.gdx.Gdx",
        "com.badlogic.gdx.Input"
    )
    )
    override fun onBackPressed() {
        // No llamar a super.onBackPressed() para evitar que la actividad cierre la aplicación
        Gdx.app.postRunnable {
            Gdx.input.inputProcessor.keyDown(Input.Keys.BACK)
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            enterImmersiveMode()
        }
    }
}
