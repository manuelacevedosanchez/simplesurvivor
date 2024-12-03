package es.masmultimedia.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2

open class Projectile(
    open var position: Vector2,
    open val direction: Vector2,
    open val speed: Float,
    open val power: Int,
    open val color: Color = Color.WHITE, // Proyectil por defecto en color blanco
    open val size: Float = 5f            // Tamaño por defecto
) {
    open fun update() {
        position.add(direction.cpy().nor().scl(speed * Gdx.graphics.deltaTime))
    }

    open fun render(shapeRenderer: ShapeRenderer) {
        shapeRenderer.color = color
        shapeRenderer.circle(position.x, position.y, size)
    }
}
