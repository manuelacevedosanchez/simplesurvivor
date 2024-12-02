package es.masmultimedia.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

class Enemy(initialPosition: Vector2) {
    var position: Vector2 = initialPosition
    private val speed = 100f
    private var health = 100

    val bounds: Rectangle
        get() = Rectangle(position.x, position.y, 20f, 20f)

    fun moveTowards(target: Vector2) {
        val direction = Vector2(target.x - position.x, target.y - position.y).nor()
        position.add(direction.scl(speed * Gdx.graphics.deltaTime))
    }

    fun takeDamage(damage: Int) {
        health -= damage
        Gdx.app.log("Enemy", "Took $damage damage, health is now $health")
    }

    fun isAlive(): Boolean {
        return health > 0
    }
}
