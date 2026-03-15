package es.masmultimedia.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils
import es.masmultimedia.utils.Constants

open class Projectile(
    open var position: Vector2,
    open val direction: Vector2,
    open val speed: Float,
    open val power: Int,
    open val color: Color = Color.WHITE, // Default projectile color
    open val size: Float = 5f, // Default projectile size
    private val maxTravelDistance: Float = Constants.MAX_PROJECTILE_TRAVEL_DISTANCE,
    private val maxLifetimeMs: Long = Constants.MAX_PROJECTILE_LIFETIME_MS
) {
    private val movementDirection = direction.cpy().apply {
        if (len2() == 0f) {
            set(1f, 0f)
        } else {
            nor()
        }
    }
    private val spawnX = position.x
    private val spawnY = position.y
    private val createdAt = TimeUtils.millis()
    private val maxTravelDistanceSquared = maxTravelDistance * maxTravelDistance

    open fun update() {
        position.mulAdd(movementDirection, speed * Gdx.graphics.deltaTime)
    }

    open fun shouldRemove(now: Long = TimeUtils.millis()): Boolean {
        if (now - createdAt > maxLifetimeMs) {
            return true
        }
        return position.dst2(spawnX, spawnY) > maxTravelDistanceSquared
    }

    open fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.color = color
        shapeRenderer.circle(position.x, position.y, size)
    }
}
