package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils

class LaserProjectile(
    val origin: Vector2,
    direction: Vector2,
    val length: Float = 1000f,
    val duration: Long = 300L,
) : Projectile(
    position = origin.cpy(),
    direction = direction.cpy(),
    speed = 0f,
    power = 500,
    color = Color.RED,
    size = 2f
) {
    private val startTime = TimeUtils.millis()
    private val hitEnemies = mutableSetOf<Enemy>()
    private val laserDirection = direction.cpy().apply {
        if (len2() == 0f) {
            set(1f, 0f)
        } else {
            nor()
        }
    }
    private val endPoint = origin.cpy()

    override fun update() {
        // The laser does not move; only its lifetime is tracked.
    }

    override fun shouldRemove(now: Long): Boolean {
        return now - startTime > duration
    }

    override fun render(shapeRenderer: ShapeRenderer) {
        val progress = (TimeUtils.timeSinceMillis(startTime).toFloat() / duration.toFloat()).coerceAtLeast(0f)
        val thickness = 3f + 3f * kotlin.math.sin(progress * Math.PI * 4).toFloat()
        val end = getEndPoint()

        var glowThickness = thickness * 3
        for (glow in GLOW_COLORS) {
            shapeRenderer.color = glow
            shapeRenderer.rectLine(origin, end, glowThickness)
            glowThickness -= 2f
        }

        shapeRenderer.color = Color.RED
        shapeRenderer.rectLine(origin, end, thickness)
    }

    fun getEndPoint(): Vector2 {
        return endPoint.set(origin).mulAdd(laserDirection, length)
    }

    fun tryHit(enemy: Enemy) {
        if (!hitEnemies.contains(enemy)) {
            enemy.takeDamage(power)
            hitEnemies.add(enemy)
        }
    }

    private companion object {
        val GLOW_COLORS = arrayOf(
            Color(1f, 0f, 0f, 0.2f),
            Color(1f, 0.5f, 0f, 0.4f),
            Color(1f, 1f, 0f, 0.6f)
        )
    }
}
