package es.masmultimedia.entities

import com.badlogic.gdx.math.Vector2
import es.masmultimedia.utils.GameAssetManager

class FastSpaceship(position: Vector2) : Spaceship(
    position = position,
    texture = GameAssetManager.getTexture("fast_spaceship.png"),
    speed = 300f,
    maxHealth = 80,
    currentHealth = 80,
    projectileType = ProjectileType.FAST
)
