package es.masmultimedia.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2
import es.masmultimedia.utils.GameAssetManager

enum class EnemyType {
    NORMAL,
    FAST,
    STRONG
}

object EnemyFactory {
    fun createEnemy(type: EnemyType, position: Vector2): Enemy {
        val texturePath = when (type) {
            EnemyType.NORMAL -> "enemy_normal.png"
            EnemyType.FAST -> "fast_enemy.png"
            EnemyType.STRONG -> "strong_enemy.png"
        }

        val texture = if (GameAssetManager.manager.isLoaded(texturePath, Texture::class.java)) {
            GameAssetManager.getTexture(texturePath)
        } else {
            Gdx.app.log("EnemyFactory", "La textura $texturePath no está cargada. Se usará una textura por defecto.")
            GameAssetManager.getDefaultTexture()
        }

        return when (type) {
            EnemyType.NORMAL -> Enemy(
                position = position,
                health = 100,
                speed = 100f,
                texture = texture
            )
            EnemyType.FAST -> FastEnemy(position, texture)
            EnemyType.STRONG -> StrongEnemy(position, texture)
        }
    }
}
