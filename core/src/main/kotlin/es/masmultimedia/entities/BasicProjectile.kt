package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

class BasicProjectile(position: Vector2, direction: Vector2) : Projectile(
    position = position,
    direction = direction,
    speed = 300f,
    power = 25,
    color = Color.RED,  // Proyectil básico de color rojo
    size = 5f           // Tamaño estándar
)
