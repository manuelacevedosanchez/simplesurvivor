package es.masmultimedia.utils

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.assets.AssetManager
import com.badlogic.gdx.graphics.Texture

object GameAssetManager {
    val manager = AssetManager()

    fun loadAssets() {
        // Lista de recursos que se usan actualmente
        val texturesToLoad = listOf(
            "spaceship_base.png",
            "basic_projectile.png"
            // Agrega otros recursos necesarios
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
