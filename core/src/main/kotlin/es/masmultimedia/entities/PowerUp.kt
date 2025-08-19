package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Circle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils

class PowerUp(
    val position: Vector2,
    val radius: Float = 10f,
    val color: Color = Color.RED,
    val type: Type = Type.HEALTH,
    private val lifetime: Long = 10000L,
) {
    // Área de colisión circular
    private val bounds = Circle(position, radius)
    private val spawnTime = TimeUtils.millis()

    fun update(delta: Float) {
        // Si quieres animarlo, por ejemplo, hacerlo rotar, mover, etc.
    }

    fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.color = color
        shapeRenderer.circle(position.x, position.y, radius)
    }

    // Verificar colisión con la nave
    fun overlapsWith(spaceship: Spaceship): Boolean {
        // Usa la distancia de la nave al centro del power-up o bounding circles
        val dist = Vector2.dst(spaceship.position.x, spaceship.position.y, position.x, position.y)
        return dist < (radius + (spaceship.width / 2f))
    }

    enum class Type {
        HEALTH, TRIPLE_SHOT, SHIELD, CHARGED_SHOT, LASER, SATELLITE
    }

    fun isExpired(): Boolean = TimeUtils.timeSinceMillis(spawnTime) > lifetime

}
