package es.masmultimedia.game

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import es.masmultimedia.screens.MainMenuScreen
import es.masmultimedia.utils.GameAssetManager

class SimpleSurvivorGame : Game() {
    lateinit var batch: SpriteBatch
    lateinit var font: BitmapFont

    override fun create() {
        batch = SpriteBatch()
        font = BitmapFont()

        // Cargar los assets aquí
        GameAssetManager.loadAssets()

        this.setScreen(MainMenuScreen(this))
    }

    override fun render() {
        super.render()
    }

    override fun dispose() {
        batch.dispose()
        font.dispose()
        // Disponer el GameAssetManager
        GameAssetManager.dispose()
    }
}
