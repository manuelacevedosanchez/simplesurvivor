package es.masmultimedia.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import es.masmultimedia.utils.GameAssetManager

open class Enemy(
    open val position: Vector2,
    open var health: Int,
    open val speed: Float,
    open val texture: Texture = GameAssetManager.getTexture("enemy_base.png"),
) {
    open val bounds: Rectangle
        get() = Rectangle(position.x, position.y, texture.width.toFloat(), texture.height.toFloat())

    open fun moveTowards(target: Vector2) {
        val direction = Vector2(target.x - position.x, target.y - position.y).nor()
        position.add(direction.scl(speed * Gdx.graphics.deltaTime))
    }

    open fun takeDamage(damage: Int) {
        health -= damage
        Gdx.app.log(this::class.simpleName, "Took $damage damage, health is now $health")
    }

    open fun isAlive(): Boolean = health > 0

    open fun render(batch: SpriteBatch) {
        batch.draw(texture, position.x, position.y)
    }
}
