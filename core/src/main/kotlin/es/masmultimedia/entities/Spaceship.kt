package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2

class Spaceship(initialPosition: Vector2) {
    var position: Vector2 = initialPosition
    var rotation: Float = 0f
    private val texture = Texture("spaceship.png")
    val width = 40f
    val height = 40f

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
