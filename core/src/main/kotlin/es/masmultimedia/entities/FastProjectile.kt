package es.masmultimedia.entities

import com.badlogic.gdx.math.Vector2
import es.masmultimedia.utils.GameAssetManager

class FastProjectile(position: Vector2, direction: Vector2) : Projectile(
    position = position,
    direction = direction,
    speed = 400f,
    power = 20,
    texture = GameAssetManager.getTexture("fast_projectile.png")
)
