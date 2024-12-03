package es.masmultimedia.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture

object GameAssetManager {
    val manager = AssetManager()

    /**
     * Loads all necessary game assets.
     *
     * This method loads all textures and other resources used in the game using LibGDX's `AssetManager`.
     * It should be called before initializing any screen or class that requires these resources to ensure
     * all assets are available when needed.
     *
     * Functionality:
     * - Defines a list of file paths for textures to load.
     * - Checks if each file exists in the assets folder before attempting to load.
     *   - If the file exists, it is loaded using the `AssetManager`.
     *   - If the file does not exist, a warning message is logged.
     * - Calls `manager.finishLoading()` to complete the loading process before continuing.
     *
     * Includes:
     * - All textures (images) used in the game, such as spaceships, enemies, backgrounds, UI elements, etc.
     * - Other resources like sounds (`Sound`), music (`Music`), custom fonts, UI skins, etc., if managed through the `AssetManager`.
     *
     * Notes:
     * - Keep the resource list updated as assets are added or removed from the game.
     * - Verifying the existence of files before loading helps avoid runtime errors due to missing files.
     * - If a resource is not available, a default texture is used to prevent failures.
     * - Resources loaded with the `AssetManager` should be disposed of at the end of the game by calling the `dispose()` method of this class.
     */
    fun loadAssets() {
        // Lista de recursos a cargar
        val texturesToLoad = listOf(
            // Texturas de naves espaciales (Spaceships)
            "spaceship_base.png",
            "fast_spaceship.png",
            "strong_spaceship.png",

            // Texturas de enemigos (Enemies)
            "enemy_normal.png",
            "fast_enemy.png",
            "strong_enemy.png",

            // Texturas de UI y fondos
            "menu_background.png",
            "logo.png",

            // Otras texturas utilizadas en el juego
            // Añadir aquí cualquier otra textura que se utilice
        )

        // Cargar texturas verificando si existen
        for (texturePath in texturesToLoad) {
            if (Gdx.files.internal(texturePath).exists()) {
                manager.load(texturePath, Texture::class.java)
            } else {
                Gdx.app.log("GameAssetManager", "El archivo $texturePath no existe. No se cargará.")
            }
        }

        // Esperar a que se carguen todos los assets
        manager.finishLoading()
    }

    fun getTexture(path: String): Texture {
        // Verificar si la textura está cargada
        if (manager.isLoaded(path, Texture::class.java)) {
            return manager.get(path, Texture::class.java)
        } else {
            // Manejar el caso donde la textura no esté cargada
            Gdx.app.log(
                "GameAssetManager",
                "La textura $path no está cargada. Se usará una textura por defecto."
            )
            return getDefaultTexture()
        }
    }

    fun getDefaultTexture(): Texture {
        // Puedes cargar una textura por defecto o crear una textura en blanco
        if (!manager.isLoaded("default.png", Texture::class.java)) {
            // Verificar si existe la textura por defecto
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
}
