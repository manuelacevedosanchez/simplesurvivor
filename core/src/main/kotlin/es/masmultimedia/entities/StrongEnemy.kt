package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2

class StrongEnemy(position: Vector2, texture: Texture) : Enemy(
    position = position,
    health = 200,
    speed = 80f,
    texture = texture
)
