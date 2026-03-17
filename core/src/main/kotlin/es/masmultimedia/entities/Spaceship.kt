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
    // === POWER-UPS DEFENSIVOS ===
    private var shieldActive = false
    private var shieldEndTime = 0L

    private var speedBoostActive = false
    private var speedBoostEndTime = 0L
    private var speedBoostMultiplier = 1.5f

    private var regenerationActive = false
    private var regenerationEndTime = 0L
    private var lastRegenTime = 0L

    private var invincibilityActive = false
    private var invincibilityEndTime = 0L

    // === POWER-UPS OFENSIVOS ===
    private var rapidFireActive = false
    private var rapidFireEndTime = 0L

    private var piercingActive = false
    private var piercingEndTime = 0L

    private var homingActive = false
    private var homingEndTime = 0L

    // === POWER-UPS DE UTILIDAD ===
    private var magnetActive = false
    private var magnetEndTime = 0L
    val magnetRange = 150f

    private var scoreMultiplierActive = false
    private var scoreMultiplierEndTime = 0L

    private var timeSlowActive = false
    private var timeSlowEndTime = 0L

    // === POWER-UPS ESPECIALES ===
    private var mirrorShotActive = false
    private var mirrorShotEndTime = 0L

    open fun updateRotation(targetPosition: Vector2) {
        rotation =
            Vector2(targetPosition.x - position.x, targetPosition.y - position.y).angleDeg() - 90
    }

    open fun updatePosition(direction: Vector2, deltaTime: Float) {
        val currentSpeed = if (speedBoostActive) speed * speedBoostMultiplier else speed
        position.add(direction.scl(currentSpeed * deltaTime))
    }

    open fun takeDamage(damage: Int) {
        if (shieldActive || invincibilityActive) return // No recibe daño
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
    }

    fun applyPowerUp(powerUp: PowerUp) {
        val now = TimeUtils.millis()
        when (powerUp.type) {
            // === OFENSIVOS ===
            PowerUp.Type.TRIPLE_SHOT -> {
                projectileType = ProjectileType.TRIPLE
            }
            PowerUp.Type.CHARGED_SHOT -> {
                projectileType = ProjectileType.CHARGED
            }
            PowerUp.Type.LASER -> {
                projectileType = ProjectileType.LASER
            }
            PowerUp.Type.RAPID_FIRE -> {
                rapidFireActive = true
                rapidFireEndTime = now + powerUp.type.duration
            }
            PowerUp.Type.PIERCING -> {
                piercingActive = true
                piercingEndTime = now + powerUp.type.duration
            }
            PowerUp.Type.HOMING -> {
                homingActive = true
                homingEndTime = now + powerUp.type.duration
            }
            PowerUp.Type.BOMB -> {
                // Se maneja en GameScreen (daño a todos los enemigos)
            }

            // === DEFENSIVOS ===
            PowerUp.Type.HEALTH -> {
                currentHealth = (currentHealth + 30).coerceAtMost(maxHealth)
            }
            PowerUp.Type.SHIELD -> {
                shieldActive = true
                shieldEndTime = now + powerUp.type.duration
            }
            PowerUp.Type.SPEED_BOOST -> {
                speedBoostActive = true
                speedBoostEndTime = now + powerUp.type.duration
            }
            PowerUp.Type.REGENERATION -> {
                regenerationActive = true
                regenerationEndTime = now + powerUp.type.duration
                lastRegenTime = now
            }
            PowerUp.Type.INVINCIBILITY -> {
                invincibilityActive = true
                invincibilityEndTime = now + powerUp.type.duration
            }

            // === UTILIDAD ===
            PowerUp.Type.MAGNET -> {
                magnetActive = true
                magnetEndTime = now + powerUp.type.duration
            }
            PowerUp.Type.SCORE_MULTIPLIER -> {
                scoreMultiplierActive = true
                scoreMultiplierEndTime = now + powerUp.type.duration
            }
            PowerUp.Type.TIME_SLOW -> {
                timeSlowActive = true
                timeSlowEndTime = now + powerUp.type.duration
            }

            // === SPECIAL ===
            PowerUp.Type.SATELLITE -> {
                // Handled in GameScreen
            }
            PowerUp.Type.DRONE -> {
                // Handled in GameScreen
            }
            PowerUp.Type.MIRROR_SHOT -> {
                mirrorShotActive = true
                mirrorShotEndTime = now + powerUp.type.duration
            }
        }
    }

    fun update(delta: Float) {
        val now = TimeUtils.millis()

        // Actualizar power-ups defensivos
        if (shieldActive && now > shieldEndTime) shieldActive = false
        if (speedBoostActive && now > speedBoostEndTime) speedBoostActive = false
        if (invincibilityActive && now > invincibilityEndTime) invincibilityActive = false

        // Regeneración: +5 HP cada segundo
        if (regenerationActive) {
            if (now > regenerationEndTime) {
                regenerationActive = false
            } else if (now - lastRegenTime >= 1000L) {
                currentHealth = (currentHealth + 5).coerceAtMost(maxHealth)
                lastRegenTime = now
            }
        }

        // Actualizar power-ups ofensivos
        if (rapidFireActive && now > rapidFireEndTime) rapidFireActive = false
        if (piercingActive && now > piercingEndTime) piercingActive = false
        if (homingActive && now > homingEndTime) homingActive = false

        // Actualizar power-ups de utilidad
        if (magnetActive && now > magnetEndTime) magnetActive = false
        if (scoreMultiplierActive && now > scoreMultiplierEndTime) scoreMultiplierActive = false
        if (timeSlowActive && now > timeSlowEndTime) timeSlowActive = false

        // Actualizar power-ups especiales
        if (mirrorShotActive && now > mirrorShotEndTime) mirrorShotActive = false
    }

    // === GETTERS PARA ESTADO DE POWER-UPS ===
    fun isShieldActive(): Boolean = shieldActive
    fun isSpeedBoostActive(): Boolean = speedBoostActive
    fun isRegenerationActive(): Boolean = regenerationActive
    fun isInvincibilityActive(): Boolean = invincibilityActive

    fun isRapidFireActive(): Boolean = rapidFireActive
    fun isPiercingActive(): Boolean = piercingActive
    fun isHomingActive(): Boolean = homingActive

    fun isMagnetActive(): Boolean = magnetActive
    fun isScoreMultiplierActive(): Boolean = scoreMultiplierActive
    fun isTimeSlowActive(): Boolean = timeSlowActive
    fun getScoreMultiplier(): Int = if (scoreMultiplierActive) 2 else 1
    fun getTimeSlowFactor(): Float = if (timeSlowActive) 0.5f else 1f

    fun isMirrorShotActive(): Boolean = mirrorShotActive

    /**
     * Gets the shot cooldown multiplier (reduced if rapid fire is active)
     */
    fun getShotCooldownMultiplier(): Float = if (rapidFireActive) 0.4f else 1f

    /**
     * Returns a list of currently active temporary power-ups with their remaining time
     */
    fun getActivePowerUps(): List<Pair<String, Long>> {
        val now = TimeUtils.millis()
        val active = mutableListOf<Pair<String, Long>>()

        if (shieldActive) active.add("SHD" to (shieldEndTime - now))
        if (speedBoostActive) active.add("SPD" to (speedBoostEndTime - now))
        if (regenerationActive) active.add("REG" to (regenerationEndTime - now))
        if (invincibilityActive) active.add("INV" to (invincibilityEndTime - now))
        if (rapidFireActive) active.add("RPD" to (rapidFireEndTime - now))
        if (piercingActive) active.add("PRC" to (piercingEndTime - now))
        if (homingActive) active.add("HOM" to (homingEndTime - now))
        if (magnetActive) active.add("MAG" to (magnetEndTime - now))
        if (scoreMultiplierActive) active.add("x2" to (scoreMultiplierEndTime - now))
        if (timeSlowActive) active.add("SLO" to (timeSlowEndTime - now))
        if (mirrorShotActive) active.add("MIR" to (mirrorShotEndTime - now))

        return active
    }
}
