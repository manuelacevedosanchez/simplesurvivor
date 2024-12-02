package es.masmultimedia.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Vector2

class Enemy(initialPosition: Vector2) {
    var position: Vector2 = initialPosition
    private val speed = 100f

    fun moveTowards(target: Vector2) {
        val direction = Vector2(target.x - position.x, target.y - position.y).nor()
        position.add(direction.scl(speed * Gdx.graphics.deltaTime))
    }
}
