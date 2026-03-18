package es.masmultimedia.utils

import com.badlogic.gdx.Gdx

/**
 * Manages all audio (sound effects and music) for the game.
 * Provides centralized volume control and easy access to play sounds.
 */
object AudioManager {

    // Volume settings (0.0 to 1.0)
    var soundVolume: Float = 1.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            saveSoundSettings()
        }

    var musicVolume: Float = 0.5f
        set(value) {
            field = value.coerceIn(0f, 1f)
            currentMusic?.volume = field
            saveSoundSettings()
        }

    var soundEnabled: Boolean = true
        set(value) {
            field = value
            saveSoundSettings()
        }

    var musicEnabled: Boolean = true
        set(value) {
            field = value
            if (!value) {
                stopAllMusic()
            }
            saveSoundSettings()
        }

    // Track currently playing music
    private var currentMusic: com.badlogic.gdx.audio.Music? = null
    private var currentMusicPath: String? = null

    // Sound effect paths
    object Sounds {
        const val SHOOT = "sounds/shoot.wav"
        const val EXPLOSION = "sounds/explosion.wav"
        const val POWERUP = "sounds/powerup.wav"
        const val HIT = "sounds/hit.wav"
        const val GAME_OVER = "sounds/game_over.wav"
        const val BUTTON_CLICK = "sounds/button_click.wav"
        const val LEVEL_UP = "sounds/level_up.wav"
        const val SHOP_PURCHASE = "sounds/shop_purchase.wav"
    }

    // Music track paths
    object Music {
        const val MENU_THEME = "music/menu_theme.ogg"
        const val GAME_THEME = "music/game_theme.ogg"
        const val GAME_OVER_THEME = "music/game_over_theme.ogg"
    }

    /**
     * Initialize audio settings from preferences
     */
    fun init() {
        val prefs = Gdx.app.getPreferences("SimpleSurvivorAudio")
        soundVolume = prefs.getFloat("soundVolume", 1.0f)
        musicVolume = prefs.getFloat("musicVolume", 0.5f)
        soundEnabled = prefs.getBoolean("soundEnabled", true)
        musicEnabled = prefs.getBoolean("musicEnabled", true)
    }

    /**
     * Save audio settings to preferences
     */
    private fun saveSoundSettings() {
        val prefs = Gdx.app.getPreferences("SimpleSurvivorAudio")
        prefs.putFloat("soundVolume", soundVolume)
        prefs.putFloat("musicVolume", musicVolume)
        prefs.putBoolean("soundEnabled", soundEnabled)
        prefs.putBoolean("musicEnabled", musicEnabled)
        prefs.flush()
    }

    /**
     * Play a sound effect
     */
    fun playSound(soundPath: String, volumeMultiplier: Float = 1f, pitch: Float = 1f) {
        if (!soundEnabled) return

        val sound = GameAssetManager.getSound(soundPath)
        if (sound != null) {
            val finalVolume = soundVolume * volumeMultiplier
            sound.play(finalVolume, pitch, 0f)
        } else {
            Gdx.app.log("AudioManager", "Sound not loaded: $soundPath")
        }
    }

    /**
     * Play a sound with random pitch variation for variety
     */
    fun playSoundWithVariation(soundPath: String, volumeMultiplier: Float = 1f, pitchVariation: Float = 0.1f) {
        val randomPitch = 1f + (Math.random().toFloat() * 2f - 1f) * pitchVariation
        playSound(soundPath, volumeMultiplier, randomPitch)
    }

    /**
     * Play music track (stops any currently playing music)
     */
    fun playMusic(musicPath: String, looping: Boolean = true) {
        if (!musicEnabled) return

        // Don't restart if same music is already playing
        if (currentMusicPath == musicPath && currentMusic?.isPlaying == true) {
            return
        }

        // Stop current music
        stopAllMusic()

        val music = GameAssetManager.getMusic(musicPath)
        if (music != null) {
            music.isLooping = looping
            music.volume = musicVolume
            music.play()
            currentMusic = music
            currentMusicPath = musicPath
        } else {
            Gdx.app.log("AudioManager", "Music not loaded: $musicPath")
        }
    }

    /**
     * Pause currently playing music
     */
    fun pauseMusic() {
        currentMusic?.pause()
    }

    /**
     * Resume paused music
     */
    fun resumeMusic() {
        if (musicEnabled) {
            currentMusic?.play()
        }
    }

    /**
     * Stop all music
     */
    fun stopAllMusic() {
        currentMusic?.stop()
        currentMusic = null
        currentMusicPath = null
    }

    /**
     * Fade out current music over duration
     */
    fun fadeOutMusic(duration: Float = 1f) {
        // Simple implementation - for more complex fading, use a tweening library
        currentMusic?.let { music ->
            val startVolume = music.volume
            val steps = 20
            val stepDuration = duration / steps
            val volumeStep = startVolume / steps

            Thread {
                for (i in 0 until steps) {
                    Thread.sleep((stepDuration * 1000).toLong())
                    Gdx.app.postRunnable {
                        music.volume = (startVolume - volumeStep * (i + 1)).coerceAtLeast(0f)
                    }
                }
                Gdx.app.postRunnable {
                    stopAllMusic()
                }
            }.start()
        }
    }

    // Convenience methods for common sounds
    fun playShoot() = playSoundWithVariation(Sounds.SHOOT, 0.6f, 0.15f)
    fun playExplosion() = playSoundWithVariation(Sounds.EXPLOSION, 0.8f, 0.2f)
    fun playPowerUp() = playSound(Sounds.POWERUP, 0.9f)
    fun playHit() = playSoundWithVariation(Sounds.HIT, 0.5f, 0.1f)
    fun playGameOver() = playSound(Sounds.GAME_OVER, 1f)
    fun playButtonClick() = playSound(Sounds.BUTTON_CLICK, 0.7f)
    fun playLevelUp() = playSound(Sounds.LEVEL_UP, 0.9f)
    fun playShopPurchase() = playSound(Sounds.SHOP_PURCHASE, 0.8f)

    // Convenience methods for music
    fun playMenuMusic() = playMusic(Music.MENU_THEME)
    fun playGameMusic() = playMusic(Music.GAME_THEME)
    fun playGameOverMusic() = playMusic(Music.GAME_OVER_THEME, looping = false)
}

