package es.masmultimedia.entities

import com.badlogic.gdx.math.Vector2
import es.masmultimedia.utils.GameAssetManager

class BasicProjectile(position: Vector2, direction: Vector2) : Projectile(
    position = position,
    direction = direction,
    speed = 300f,
    power = 25,
    texture = GameAssetManager.getTexture("basic_projectile.png")
)
