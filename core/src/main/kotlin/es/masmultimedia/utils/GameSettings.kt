package es.masmultimedia.utils

import com.badlogic.gdx.Gdx

/**
 * Centralized game settings stored in preferences.
 * Covers display, controls and data management options.
 */
object GameSettings {

    private const val PREFS_NAME = "SimpleSurvivorSettings"

    // --- Show FPS counter ---
    var showFps: Boolean = false
        set(value) {
            field = value
            save()
        }

    // --- Joystick size multiplier (0.5 – 1.5, default 1.0) ---
    var joystickSize: Float = 1.0f
        set(value) {
            field = value.coerceIn(0.5f, 1.5f)
            save()
        }

    // --- Vibration enabled ---
    var vibrationEnabled: Boolean = true
        set(value) {
            field = value
            save()
        }

    fun init() {
        val prefs = Gdx.app.getPreferences(PREFS_NAME)
        showFps = prefs.getBoolean("showFps", false)
        joystickSize = prefs.getFloat("joystickSize", 1.0f)
        vibrationEnabled = prefs.getBoolean("vibrationEnabled", true)
    }

    private fun save() {
        val prefs = Gdx.app.getPreferences(PREFS_NAME)
        prefs.putBoolean("showFps", showFps)
        prefs.putFloat("joystickSize", joystickSize)
        prefs.putBoolean("vibrationEnabled", vibrationEnabled)
        prefs.flush()
    }

    /**
     * Trigger a short vibration if enabled.
     * Duration in milliseconds.
     */
    fun vibrate(durationMs: Int = 50) {
        if (!vibrationEnabled) return
        try {
            Gdx.input.vibrate(durationMs)
        } catch (_: Exception) {
            // Vibration not supported on this device
        }
    }
}

