package es.masmultimedia.entities

import com.badlogic.gdx.math.Vector2
import es.masmultimedia.utils.GameAssetManager

class StrongSpaceship(position: Vector2) : Spaceship(
    position = position,
    texture = GameAssetManager.getTexture("strong_spaceship.png"),
    speed = 150f,
    maxHealth = 150,
    currentHealth = 150,
    projectileType = ProjectileType.POWERFUL
)
