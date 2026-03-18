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

    /**
     * Power-up categories for organizing and balancing drops
     */
    enum class Category {
        OFFENSIVE,  // Attack improvements
        DEFENSIVE,  // Protection and survival
        UTILITY,    // Various utilities
        SPECIAL     // Unique effects
    }

    /**
     * Available power-up types with balanced rarity weights.
     * Higher rarity = more common. Permanent power-ups are rarer.
     */
    enum class Type(val category: Category, val duration: Long, val rarity: Float) {
        // === OFFENSIVE ===
        TRIPLE_SHOT(Category.OFFENSIVE, -1L, 0.06f),           // Permanent, rare
        CHARGED_SHOT(Category.OFFENSIVE, -1L, 0.05f),          // Permanent, rare
        LASER(Category.OFFENSIVE, -1L, 0.04f),                 // Permanent, very rare
        RAPID_FIRE(Category.OFFENSIVE, 8000L, 0.10f),          // 8 sec, uncommon
        PIERCING(Category.OFFENSIVE, 10000L, 0.08f),           // 10 sec, uncommon
        HOMING(Category.OFFENSIVE, 8000L, 0.05f),              // 8 sec, rare
        BOMB(Category.OFFENSIVE, 0L, 0.03f),                   // Instant, very rare

        // === DEFENSIVE ===
        HEALTH(Category.DEFENSIVE, 0L, 0.25f),                 // Instant, very common
        SHIELD(Category.DEFENSIVE, 5000L, 0.10f),              // 5 sec, uncommon
        SPEED_BOOST(Category.DEFENSIVE, 6000L, 0.12f),         // 6 sec, common
        REGENERATION(Category.DEFENSIVE, 8000L, 0.06f),        // 8 sec, rare
        INVINCIBILITY(Category.DEFENSIVE, 3000L, 0.03f),       // 3 sec, very rare

        // === UTILITY ===
        MAGNET(Category.UTILITY, 10000L, 0.08f),               // 10 sec, uncommon
        SCORE_MULTIPLIER(Category.UTILITY, 15000L, 0.07f),     // 15 sec, uncommon
        TIME_SLOW(Category.UTILITY, 5000L, 0.04f),             // 5 sec, rare

        // === SPECIAL ===
        SATELLITE(Category.SPECIAL, -1L, 0.04f),               // Permanent, very rare
        DRONE(Category.SPECIAL, -1L, 0.03f),                   // Permanent, very rare
        MIRROR_SHOT(Category.SPECIAL, 12000L, 0.05f);          // 12 sec, rare

        fun isPermanent(): Boolean = duration == -1L
        fun isInstant(): Boolean = duration == 0L
        fun isTimed(): Boolean = duration > 0L
    }

    fun isExpired(): Boolean = TimeUtils.timeSinceMillis(spawnTime) > lifetime

    companion object {
        /**
         * Gets the color associated with each power-up type
         */
        fun getColor(type: Type): Color = when (type) {
            // Offensive - red/orange tones
            Type.TRIPLE_SHOT -> Color.RED
            Type.CHARGED_SHOT -> Color.ORANGE
            Type.LASER -> Color.BLUE
            Type.RAPID_FIRE -> Color.CORAL
            Type.PIERCING -> Color.MAROON
            Type.HOMING -> Color.SALMON
            Type.BOMB -> Color.FIREBRICK

            // Defensive - green/cyan tones
            Type.HEALTH -> Color.GREEN
            Type.SHIELD -> Color.CYAN
            Type.SPEED_BOOST -> Color.LIME
            Type.REGENERATION -> Color.FOREST
            Type.INVINCIBILITY -> Color.GOLD

            // Utility - purple/magenta tones
            Type.MAGNET -> Color.MAGENTA
            Type.SCORE_MULTIPLIER -> Color.VIOLET
            Type.TIME_SLOW -> Color.PURPLE

            // Special - white/gray tones
            Type.SATELLITE -> Color.WHITE
            Type.DRONE -> Color.LIGHT_GRAY
            Type.MIRROR_SHOT -> Color.SKY
        }
    }
}
