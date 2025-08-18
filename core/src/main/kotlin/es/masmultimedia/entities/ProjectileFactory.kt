package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

enum class ProjectileType {
    BASIC,
    FAST,
    POWERFUL,
    CHARGED,
}

object ProjectileFactory {
    fun createProjectile(type: ProjectileType, position: Vector2, direction: Vector2): Projectile {
        return when (type) {
            ProjectileType.BASIC -> BasicProjectile(position, direction)
            ProjectileType.FAST -> FastProjectile(position, direction)
            ProjectileType.POWERFUL -> PowerfulProjectile(position, direction)
            ProjectileType.CHARGED -> Projectile(
                position = position,
                direction = direction,
                speed = 400f,    // más lento que el normal
                power = 50,      // mucho más daño
                color = Color.YELLOW,
                size = 8f      // bola más grande
            )
        }
    }
}
