package es.masmultimedia.entities

import com.badlogic.gdx.math.Vector2

enum class ProjectileType {
    BASIC,
    FAST,
    POWERFUL
}

object ProjectileFactory {
    fun createProjectile(type: ProjectileType, position: Vector2, direction: Vector2): Projectile {
        return when (type) {
            ProjectileType.BASIC -> BasicProjectile(position, direction)
            ProjectileType.FAST -> FastProjectile(position, direction)
            ProjectileType.POWERFUL -> PowerfulProjectile(position, direction)
        }
    }
}
