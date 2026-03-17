package es.masmultimedia.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import es.masmultimedia.utils.GameAssetManager
import kotlin.math.sqrt

open class Enemy(
    open val position: Vector2,
    open var health: Int,
    open val speed: Float,
    open val type: EnemyType,
    open val texture: Texture = GameAssetManager.getTexture("enemy_base.png"),
) {
    private val boundsRect = Rectangle()

    open val bounds: Rectangle
        get() = boundsRect.set(position.x, position.y, texture.width.toFloat(), texture.height.toFloat())

    open fun moveTowards(target: Vector2, speedFactor: Float = 1f) {
        val dx = target.x - position.x
        val dy = target.y - position.y
        val distanceSquared = dx * dx + dy * dy
        if (distanceSquared == 0f) {
            return
        }

        val scale = speed * speedFactor * Gdx.graphics.deltaTime / sqrt(distanceSquared)
        position.x += dx * scale
        position.y += dy * scale
    }

    open fun takeDamage(damage: Int) {
        health -= damage
    }

    open fun isAlive(): Boolean = health > 0

    open fun render(batch: SpriteBatch) {
        batch.draw(texture, position.x, position.y)
    }
}
