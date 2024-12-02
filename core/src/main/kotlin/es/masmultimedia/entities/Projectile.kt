package es.masmultimedia.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2

class Projectile(initialPosition: Vector2, private val direction: Vector2) {
    var position: Vector2 = initialPosition
    private val speed = 300f

    fun update() {
        position.add(direction.cpy().nor().scl(speed * Gdx.graphics.deltaTime))
    }
}
