package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils

class LaserProjectile(
    val origin: Vector2,
    direction: Vector2,
    val length: Float = 1000f,  // Largo del láser
    val duration: Long = 300L,  // Duración en ms
) : Projectile(
    position = origin.cpy(),
    direction = direction.cpy(),
    speed = 0f, // no se mueve
    power = 500,
    color = Color.RED,
    size = 2f
) {
    private val startTime = TimeUtils.millis()
    private val hitEnemies = mutableSetOf<Enemy>() // enemigos ya dañados

    override fun update() {
        // El láser no se mueve, solo se mide el tiempo de vida
    }

    fun isExpired(): Boolean {
        return TimeUtils.timeSinceMillis(startTime) > duration
    }

    override fun render(shapeRenderer: ShapeRenderer) {
        val progress = TimeUtils.timeSinceMillis(startTime).toFloat() / duration.toFloat()

        // Grosor pulsante entre 3 y 6 px
        val thickness = 3f + 3f * kotlin.math.sin(progress * Math.PI * 4).toFloat()

        val end = getEndPoint()

        // Glow falso: varias capas
        val glowColors = listOf(
            Color(1f, 0f, 0f, 0.2f), // rojo muy transparente
            Color(1f, 0.5f, 0f, 0.4f), // naranja
            Color(1f, 1f, 0f, 0.6f)    // amarillo más sólido
        )

        var glowThickness = thickness * 3
        for (glow in glowColors) {
            shapeRenderer.color = glow
            shapeRenderer.rectLine(origin, end, glowThickness)
            glowThickness -= 2f
        }

        // Línea central sólida (roja brillante)
        shapeRenderer.color = Color.RED
        shapeRenderer.rectLine(origin, end, thickness)
    }

    fun getEndPoint(): Vector2 {
        return origin.cpy().add(direction.cpy().nor().scl(length))
    }

    fun tryHit(enemy: Enemy) {
        if (!hitEnemies.contains(enemy)) {
            enemy.takeDamage(power)
            hitEnemies.add(enemy)
        }
    }

}
