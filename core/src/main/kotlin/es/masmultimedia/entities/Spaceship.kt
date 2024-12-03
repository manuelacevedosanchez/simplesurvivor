package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import es.masmultimedia.utils.GameAssetManager

open class Spaceship(
    open var position: Vector2,
    open var rotation: Float = 0f,
    open val texture: Texture = GameAssetManager.getTexture("spaceship_base.png"),
    open val width: Float = 40f,
    open val height: Float = 40f,
    open val maxHealth: Int = 100,
    open var currentHealth: Int = 100,
    open val speed: Float = 200f,
    open val projectileType: ProjectileType = ProjectileType.BASIC
) {
    open fun updateRotation(targetPosition: Vector2) {
        rotation =
            Vector2(targetPosition.x - position.x, targetPosition.y - position.y).angleDeg() - 90
    }

    open fun updatePosition(direction: Vector2, deltaTime: Float) {
        position.add(direction.scl(speed * deltaTime))
    }

    open fun takeDamage(damage: Int) {
        currentHealth -= damage
        if (currentHealth < 0) currentHealth = 0
    }

    open fun isAlive(): Boolean = currentHealth > 0

    open fun render(batch: SpriteBatch) {
        batch.draw(
            texture,
            position.x - width / 2,
            position.y - height / 2,
            width / 2,
            height / 2,
            width,
            height,
            1f,
            1f,
            rotation,
            0,
            0,
            texture.width,
            texture.height,
            false,
            false
        )
    }

    open fun dispose() {
        texture.dispose()
    }
}
