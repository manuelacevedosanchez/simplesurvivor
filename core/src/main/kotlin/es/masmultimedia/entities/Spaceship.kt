package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2

class Spaceship(initialPosition: Vector2) {
    var position: Vector2 = initialPosition
    var rotation: Float = 0f
    private val texture = Texture("spaceship_base.png")
    val width = 40f
    val height = 40f

    // Atributos de salud
    val maxHealth = 100
    var currentHealth = maxHealth

    fun updateRotation(targetPosition: Vector2) {
        rotation =
            Vector2(targetPosition.x - position.x, targetPosition.y - position.y).angleDeg() - 90
    }

    fun updatePosition(direction: Vector2, speed: Float, deltaTime: Float) {
        position.add(direction.scl(speed * deltaTime))
    }

    fun takeDamage(damage: Int) {
        currentHealth -= damage
        if (currentHealth < 0) currentHealth = 0
    }

    fun isAlive(): Boolean {
        return currentHealth > 0
    }

    fun render(batch: SpriteBatch) {
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

    fun dispose() {
        texture.dispose()
    }
}
