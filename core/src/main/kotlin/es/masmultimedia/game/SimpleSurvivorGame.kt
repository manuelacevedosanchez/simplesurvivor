package es.masmultimedia.game

import com.badlogic.gdx.Game
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import es.masmultimedia.screens.LoadingScreen
import es.masmultimedia.utils.AudioManager
import es.masmultimedia.utils.GameAssetManager
import es.masmultimedia.utils.GameSettings
import es.masmultimedia.utils.I18n

class SimpleSurvivorGame : Game() {
    lateinit var batch: SpriteBatch
    lateinit var font: BitmapFont

    override fun create() {
        batch = SpriteBatch()
        font = BitmapFont()

        // Initialize audio settings from saved preferences
        AudioManager.init()

        // Initialize game settings from saved preferences
        GameSettings.init()

        // Initialize internationalization
        I18n.init()

        // Start with loading screen - assets will be loaded asynchronously
        this.setScreen(LoadingScreen(this))
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
