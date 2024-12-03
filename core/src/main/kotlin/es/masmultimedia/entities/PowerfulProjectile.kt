package es.masmultimedia.entities

import com.badlogic.gdx.math.Vector2
import es.masmultimedia.utils.GameAssetManager

class PowerfulProjectile(position: Vector2, direction: Vector2) : Projectile(
    position = position,
    direction = direction,
    speed = 250f,
    power = 50,
    texture = GameAssetManager.getTexture("powerful_projectile.png")
)
