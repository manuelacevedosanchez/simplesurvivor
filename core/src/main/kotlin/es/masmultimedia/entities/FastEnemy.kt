package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.math.Vector2

class FastEnemy(position: Vector2, texture: Texture) : Enemy(
    position = position,
    health = 50,
    speed = 150f,
    texture = texture
)
