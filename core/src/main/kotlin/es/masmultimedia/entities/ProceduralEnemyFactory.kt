package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

/**
 * Factory for creating procedurally generated enemies.
 * Each enemy type has distinct visual characteristics and stats.
 */
object ProceduralEnemyFactory {

    /**
     * Creates a procedural enemy based on type with appropriate visuals and stats.
     */
    fun createEnemy(type: EnemyType, position: Vector2): ProceduralEnemy {
        return when (type) {
            EnemyType.NORMAL -> createNormalEnemy(position)
            EnemyType.FAST -> createFastEnemy(position)
            EnemyType.STRONG -> createStrongEnemy(position)
        }
    }

    /**
     * Normal enemy: Balanced stats, circular shape, red color
     */
    private fun createNormalEnemy(position: Vector2): ProceduralEnemy {
        return ProceduralEnemy(
            position = position,
            health = 100,
            maxHealth = 100,
            speed = 100f,
            type = EnemyType.NORMAL,
            shape = EnemyShape.CIRCLE,
            size = 18f,
            primaryColor = Color.valueOf("E53935"),    // Red
            secondaryColor = Color.valueOf("FFCDD2"),  // Light red
            glowColor = Color.valueOf("FF5722")        // Deep orange
        )
    }

    /**
     * Fast enemy: Low health, high speed, diamond shape, cyan color
     */
    private fun createFastEnemy(position: Vector2): ProceduralEnemy {
        return ProceduralEnemy(
            position = position,
            health = 50,
            maxHealth = 50,
            speed = 180f,
            type = EnemyType.FAST,
            shape = EnemyShape.DIAMOND,
            size = 14f,
            primaryColor = Color.valueOf("00BCD4"),    // Cyan
            secondaryColor = Color.valueOf("E0F7FA"),  // Light cyan
            glowColor = Color.valueOf("00E5FF")        // Bright cyan
        )
    }

    /**
     * Strong enemy: High health, slow speed, square shape, purple color
     */
    private fun createStrongEnemy(position: Vector2): ProceduralEnemy {
        return ProceduralEnemy(
            position = position,
            health = 250,
            maxHealth = 250,
            speed = 60f,
            type = EnemyType.STRONG,
            shape = EnemyShape.SQUARE,
            size = 28f,
            primaryColor = Color.valueOf("7B1FA2"),    // Purple
            secondaryColor = Color.valueOf("E1BEE7"),  // Light purple
            glowColor = Color.valueOf("AA00FF")        // Bright purple
        )
    }

    /**
     * Creates a random variant of an enemy type with slightly different colors.
     * Useful for visual variety in waves.
     */
    fun createVariantEnemy(type: EnemyType, position: Vector2, variant: Int = 0): ProceduralEnemy {
        val base = createEnemy(type, position)

        // Apply color variation based on variant number
        val hueShift = (variant % 5) * 0.1f

        return ProceduralEnemy(
            position = position,
            health = base.health,
            maxHealth = base.maxHealth,
            speed = base.speed,
            type = base.type,
            shape = base.shape,
            size = base.size,
            primaryColor = shiftHue(base.primaryColor, hueShift),
            secondaryColor = base.secondaryColor,
            glowColor = shiftHue(base.glowColor, hueShift)
        )
    }

    /**
     * Creates a special/boss variant with enhanced visuals.
     */
    fun createEliteEnemy(type: EnemyType, position: Vector2): ProceduralEnemy {
        val healthMultiplier = 2.5f
        val sizeMultiplier = 1.4f

        return when (type) {
            EnemyType.NORMAL -> ProceduralEnemy(
                position = position,
                health = (100 * healthMultiplier).toInt(),
                maxHealth = (100 * healthMultiplier).toInt(),
                speed = 80f,
                type = EnemyType.NORMAL,
                shape = EnemyShape.HEXAGON,
                size = 18f * sizeMultiplier,
                primaryColor = Color.valueOf("FFD700"),    // Gold
                secondaryColor = Color.valueOf("FFF8E1"),
                glowColor = Color.valueOf("FFAB00")
            )
            EnemyType.FAST -> ProceduralEnemy(
                position = position,
                health = (50 * healthMultiplier).toInt(),
                maxHealth = (50 * healthMultiplier).toInt(),
                speed = 200f,
                type = EnemyType.FAST,
                shape = EnemyShape.STAR,
                size = 14f * sizeMultiplier,
                primaryColor = Color.valueOf("00FFFF"),    // Bright cyan
                secondaryColor = Color.valueOf("FFFFFF"),
                glowColor = Color.valueOf("00BFFF")
            )
            EnemyType.STRONG -> ProceduralEnemy(
                position = position,
                health = (250 * healthMultiplier).toInt(),
                maxHealth = (250 * healthMultiplier).toInt(),
                speed = 50f,
                type = EnemyType.STRONG,
                shape = EnemyShape.HEXAGON,
                size = 28f * sizeMultiplier,
                primaryColor = Color.valueOf("4A148C"),    // Deep purple
                secondaryColor = Color.valueOf("CE93D8"),
                glowColor = Color.valueOf("EA80FC")
            )
        }
    }

    /**
     * Shifts the hue of a color slightly for variation.
     */
    private fun shiftHue(color: Color, amount: Float): Color {
        // Slightly modify the color channels for variation
        return Color(
            (color.r + amount).coerceIn(0f, 1f),
            (color.g + amount * 0.5f).coerceIn(0f, 1f),
            (color.b - amount * 0.3f).coerceIn(0f, 1f),
            color.a
        )
    }
}

