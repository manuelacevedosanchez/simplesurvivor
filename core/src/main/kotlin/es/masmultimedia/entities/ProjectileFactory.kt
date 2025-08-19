package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

enum class ProjectileType {
    BASIC,
    FAST,
    POWERFUL,
    CHARGED,
    TRIPLE,
}

object ProjectileFactory {
    fun createProjectiles(
        type: ProjectileType,
        position: Vector2,
        direction: Vector2
    ): List<Projectile> {
        return when (type) {
            ProjectileType.BASIC -> listOf(BasicProjectile(position, direction))

            ProjectileType.FAST -> listOf(FastProjectile(position, direction))

            ProjectileType.POWERFUL -> listOf(PowerfulProjectile(position, direction))

            ProjectileType.CHARGED -> listOf(
                Projectile(
                    position = position,
                    direction = direction,
                    speed = 400f,
                    power = 50,
                    color = Color.YELLOW,
                    size = 8f
                )
            )

            ProjectileType.TRIPLE -> {
                val offset = 10f
                val center = BasicProjectile(position.cpy(), direction)
                val left = BasicProjectile(
                    position.cpy().add(-offset, 0f),
                    direction.cpy().rotateDeg(-10f)
                )
                val right =
                    BasicProjectile(position.cpy().add(offset, 0f), direction.cpy().rotateDeg(10f))
                listOf(center, left, right)
            }

        }
    }
}

