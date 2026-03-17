package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils
import es.masmultimedia.utils.Constants
import es.masmultimedia.utils.GameAssetManager

/**
 * Drone que sigue al jugador y dispara automáticamente.
 * A diferencia del Satellite que orbita, el Drone sigue al jugador con un offset.
 */
class Drone(
    private val player: Spaceship,
    private val droneIndex: Int = 0, // Para posicionar múltiples drones
    private val texture: Texture = GameAssetManager.getTexture("satellite.png"), // Reutilizamos textura
    private val fireInterval: Long = 800L // Dispara más rápido que el satellite
) {
    var position = Vector2(player.position.x, player.position.y)
    private var lastShotTime = 0L
    private val shotDirection = Vector2()

    // Offset basado en el índice del drone (para múltiples drones)
    private val offsetAngle = when (droneIndex % 4) {
        0 -> 45f
        1 -> 135f
        2 -> 225f
        else -> 315f
    }
    private val followDistance = 50f
    private val followSpeed = 8f // Velocidad de seguimiento suave

    fun update(delta: Float, enemies: List<Enemy>, projectiles: MutableList<Projectile>) {
        // Calcular posición objetivo (detrás/al lado del jugador)
        val rad = Math.toRadians((player.rotation + offsetAngle).toDouble())
        val targetX = player.position.x - followDistance * kotlin.math.cos(rad).toFloat()
        val targetY = player.position.y - followDistance * kotlin.math.sin(rad).toFloat()

        // Seguir suavemente la posición objetivo
        position.x += (targetX - position.x) * followSpeed * delta
        position.y += (targetY - position.y) * followSpeed * delta

        // Encontrar enemigo más cercano y disparar
        val target = enemies.minByOrNull { it.position.dst2(position) }
        if (
            target != null &&
            projectiles.size < Constants.MAX_ACTIVE_PROJECTILES &&
            TimeUtils.timeSinceMillis(lastShotTime) > fireInterval
        ) {
            shotDirection.set(target.position).sub(position)
            if (shotDirection.len2() > 0f) {
                projectiles.add(BasicProjectile(position.cpy(), shotDirection))
                lastShotTime = TimeUtils.millis()
            }
        }
    }

    fun render(batch: SpriteBatch) {
        // Slightly different tint than satellite to distinguish it
        batch.color = Color.LIGHT_GRAY
        batch.draw(
            texture,
            position.x - 12f,
            position.y - 12f,
            12f,
            12f,
            24f,
            24f,
            1f,
            1f,
            player.rotation, // Rotates with player
            0,
            0,
            texture.width,
            texture.height,
            false,
            false
        )
        batch.color = Color.WHITE
    }
}

