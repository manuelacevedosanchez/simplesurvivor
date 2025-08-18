package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

class PowerfulProjectile(position: Vector2, direction: Vector2) : Projectile(
    position = position,
    direction = direction,
    speed = 250f,
    power = 50,
    color = Color.BLUE, // Proyectil poderoso de color azul
    size = 6f           // Tamaño ligeramente más grande
)
