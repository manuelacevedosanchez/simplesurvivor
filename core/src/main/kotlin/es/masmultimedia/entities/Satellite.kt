package es.masmultimedia.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils
import es.masmultimedia.utils.GameAssetManager
import kotlin.math.cos
import kotlin.math.sin

class Satellite(
    private val player: Spaceship,
    private val texture: Texture = GameAssetManager.getTexture("satellite.png"),
    private val orbitRadius: Float = 60f,
    private val fireInterval: Long = 1000L // 1 segundo entre disparos
) {
    private var angle = 0f
    private var lastShotTime = 0L
    var position = Vector2(player.position.x, player.position.y)

    init {
        Gdx.app.log("Satellite", "Texture size: ${texture.width}x${texture.height}")
    }

    fun update(delta: Float, enemies: List<Enemy>, projectiles: MutableList<Projectile>) {
        // Orbitar alrededor del jugador
        angle += 90f * delta // velocidad angular en grados/segundo
        val rad = Math.toRadians(angle.toDouble())
        position.set(
            player.position.x + orbitRadius * cos(rad).toFloat(),
            player.position.y + orbitRadius * sin(rad).toFloat()
        )

        // Buscar enemigo más cercano
        val target = enemies.minByOrNull { it.position.dst2(position) }
        if (target != null && TimeUtils.timeSinceMillis(lastShotTime) > fireInterval) {
            val dir = target.position.cpy().sub(position).nor()
            projectiles.add(BasicProjectile(position.cpy(), dir))
            lastShotTime = TimeUtils.millis()
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
