package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils
import es.masmultimedia.utils.Constants
import es.masmultimedia.utils.GameAssetManager
import kotlin.math.cos
import kotlin.math.sin

class Satellite(
    private val player: Spaceship,
    private val texture: Texture = GameAssetManager.getTexture("satellite.png"),
    private val orbitRadius: Float = 60f,
    private val fireInterval: Long = 1000L // 1 second between shots
) {
    private var angle = 0f
    private var lastShotTime = 0L
    private val shotDirection = Vector2()
    var position = Vector2(player.position.x, player.position.y)

    fun update(delta: Float, enemies: List<ProceduralEnemy>, projectiles: MutableList<Projectile>) {
        // Orbit around the player
        angle += 90f * delta // Angular speed in degrees per second
        val rad = Math.toRadians(angle.toDouble())
        position.set(
            player.position.x + orbitRadius * cos(rad).toFloat(),
            player.position.y + orbitRadius * sin(rad).toFloat()
        )

        // Find the nearest enemy
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
        batch.draw(
            texture,
            position.x - 15f,
            position.y - 15f,
            15f,
            15f,
            30f,
            30f,
            1f,
            1f,
            0f,
            0,
            0,
            texture.width,
            texture.height,
            false,
            false
        )
        batch.color = com.badlogic.gdx.graphics.Color.WHITE
    }
}
