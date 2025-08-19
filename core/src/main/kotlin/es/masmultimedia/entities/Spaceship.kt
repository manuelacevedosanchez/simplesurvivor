package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.TimeUtils
import es.masmultimedia.utils.GameAssetManager

open class Spaceship(
    open var position: Vector2,
    open var rotation: Float = 0f,
    open val texture: Texture = GameAssetManager.getTexture("spaceship_base.png"),
    open val width: Float = 40f,
    open val height: Float = 40f,
    open val maxHealth: Int = 100,
    open var currentHealth: Int = 100,
    open val speed: Float = 200f,
    open var projectileType: ProjectileType = ProjectileType.BASIC
) {
    private var tripleShotActive = false
    private var tripleShotEndTime = 0L

    private var shieldActive = false
    private var shieldEndTime = 0L
    private val shieldDuration = 5000L // 5 segundos de escudo

    private var chargedShotActive = false
    private var chargedShotEndTime = 0L

    open fun updateRotation(targetPosition: Vector2) {
        rotation =
            Vector2(targetPosition.x - position.x, targetPosition.y - position.y).angleDeg() - 90
    }

    open fun updatePosition(direction: Vector2, deltaTime: Float) {
        position.add(direction.scl(speed * deltaTime))
    }

    open fun takeDamage(damage: Int) {
        if (shieldActive) return // No recibe daño
        currentHealth -= damage
        if (currentHealth < 0) currentHealth = 0
    }

    open fun isAlive(): Boolean = currentHealth > 0

    open fun render(batch: SpriteBatch) {
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

    open fun dispose() {
        // El GameAssetManager es quien gestiona la textura.
        // texture.dispose() // <- Eliminar esta línea
    }

    fun applyPowerUp(powerUp: PowerUp) {
        when (powerUp.type) {
            PowerUp.Type.HEALTH -> currentHealth = (currentHealth + 30).coerceAtMost(maxHealth)
            PowerUp.Type.TRIPLE_SHOT -> {
                projectileType = ProjectileType.TRIPLE
            }

            PowerUp.Type.SHIELD -> {
                shieldActive = true
                shieldEndTime = TimeUtils.millis() + shieldDuration
            }

            PowerUp.Type.CHARGED_SHOT -> {
                projectileType = ProjectileType.CHARGED
            }

            PowerUp.Type.LASER -> {
                projectileType = ProjectileType.LASER
            }
        }
    }

    fun update(delta: Float) {
        if (shieldActive && TimeUtils.millis() > shieldEndTime) {
            shieldActive = false
        }
    }

    fun isTripleShotActive(): Boolean = tripleShotActive

    fun isShieldActive(): Boolean = shieldActive

}
