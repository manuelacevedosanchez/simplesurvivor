package es.masmultimedia.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Texture

object GameAssetManager {
    val manager = AssetManager()

    // List of all texture assets
    private val texturesToLoad = listOf(
        // Spaceship textures
        "spaceship_base.png",
        "fast_spaceship.png",
        "strong_spaceship.png",

        // Enemy textures
        "enemy_normal.png",
        "fast_enemy.png",
        "strong_enemy.png",

        // UI and background textures
        "menu_background.png",
        "game_over_background.png",
        "background.png",
        "logo.png",

        // Other textures
        "satellite.png",
    )

    // List of sound effects
    private val soundsToLoad = listOf(
        "sounds/shoot.wav",
        "sounds/explosion.wav",
        "sounds/powerup.wav",
        "sounds/hit.wav",
        "sounds/game_over.wav",
        "sounds/button_click.wav",
        "sounds/level_up.wav",
        "sounds/shop_purchase.wav",
    )

    // List of music tracks
    private val musicToLoad = listOf(
        "music/menu_theme.ogg",
        "music/game_theme.ogg",
        "music/game_over_theme.ogg",
    )

    /**
     * Queues all assets for asynchronous loading.
     * Call manager.update() in render loop to progress loading.
     * Use manager.isFinished to check completion.
     */
    fun queueAssets() {
        // Queue textures
        for (texturePath in texturesToLoad) {
            if (Gdx.files.internal(texturePath).exists()) {
                manager.load(texturePath, Texture::class.java)
            } else {
                Gdx.app.log("GameAssetManager", "Texture $texturePath does not exist. Skipping.")
            }
        }

        // Queue sounds
        for (soundPath in soundsToLoad) {
            if (Gdx.files.internal(soundPath).exists()) {
                manager.load(soundPath, Sound::class.java)
            } else {
                Gdx.app.log("GameAssetManager", "Sound $soundPath does not exist. Skipping.")
            }
        }

        // Queue music
        for (musicPath in musicToLoad) {
            if (Gdx.files.internal(musicPath).exists()) {
                manager.load(musicPath, Music::class.java)
            } else {
                Gdx.app.log("GameAssetManager", "Music $musicPath does not exist. Skipping.")
            }
        }
    }

    /**
     * Loads all necessary game assets synchronously (blocking).
     * @deprecated Use queueAssets() with async loading instead.
     */
    fun loadAssets() {
        queueAssets()
        // Wait for all assets to load
        manager.finishLoading()
    }

    fun getTexture(path: String): Texture {
        // If already loaded, return it
        if (manager.isLoaded(path, Texture::class.java)) {
            return manager.get(path, Texture::class.java)
        }

        // If not loaded, try to load it on demand
        if (Gdx.files.internal(path).exists()) {
            Gdx.app.log("GameAssetManager", "Loading texture on demand: $path")
            manager.load(path, Texture::class.java)
            manager.finishLoadingAsset<Texture>(path) // Wait only for this asset
            return manager.get(path, Texture::class.java)
        }

        // If it doesn't exist in assets, return default texture
        Gdx.app.log("GameAssetManager", "Texture $path does not exist. Using default texture.")
        return getDefaultTexture()
    }

    fun getDefaultTexture(): Texture {
        // Load a default texture or create a blank one
        if (!manager.isLoaded("default.png", Texture::class.java)) {
            // Check if default texture exists
            if (Gdx.files.internal("default.png").exists()) {
                manager.load("default.png", Texture::class.java)
                manager.finishLoadingAsset("default.png")
            } else {
                // Crear una textura en blanco si no existe default.png
                val pixmap = com.badlogic.gdx.graphics.Pixmap(
                    64,
                    64,
                    com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888
                )
                pixmap.setColor(com.badlogic.gdx.graphics.Color.PINK)
                pixmap.fill()
                val texture = Texture(pixmap)
                pixmap.dispose()
                return texture
            }
        }
        return manager.get("default.png", Texture::class.java)
    }

    fun dispose() {
        manager.dispose()
    }

    // Sound helper methods
    fun getSound(path: String): Sound? {
        return if (manager.isLoaded(path, Sound::class.java)) {
            manager.get(path, Sound::class.java)
        } else {
            null
        }
    }

    fun playSound(path: String, volume: Float = 1f): Long {
        return getSound(path)?.play(volume) ?: -1L
    }

    // Music helper methods
    fun getMusic(path: String): Music? {
        return if (manager.isLoaded(path, Music::class.java)) {
            manager.get(path, Music::class.java)
        } else {
            null
        }
    }

    fun playMusic(path: String, looping: Boolean = true, volume: Float = 0.5f) {
        getMusic(path)?.apply {
            isLooping = looping
            this.volume = volume
            play()
        }
    }

    fun stopMusic(path: String) {
        getMusic(path)?.stop()
    }

    fun stopAllMusic() {
        for (musicPath in musicToLoad) {
            stopMusic(musicPath)
        }
    }
}
