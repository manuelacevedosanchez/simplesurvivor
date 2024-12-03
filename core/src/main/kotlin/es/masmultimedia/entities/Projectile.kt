package es.masmultimedia.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.math.Vector2
import es.masmultimedia.utils.GameAssetManager

open class Projectile(
    open var position: Vector2,
    open val direction: Vector2,
    open val speed: Float,
    open val power: Int,
    open val texture: Texture = GameAssetManager.getTexture("projecttile_base.png"),
) {
    open fun update() {
        position.add(direction.cpy().nor().scl(speed * Gdx.graphics.deltaTime))
    }

    open fun render(batch: SpriteBatch) {
        batch.draw(texture, position.x, position.y)
    }
}
