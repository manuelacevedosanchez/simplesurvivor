package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.math.Vector2

class FastProjectile(position: Vector2, direction: Vector2) : Projectile(
    position = position,
    direction = direction,
    speed = 400f,
    power = 20,
    color = Color.YELLOW, // Proyectil rápido de color amarillo
    size = 4f             // Tamaño ligeramente más pequeño
)
