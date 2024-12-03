package es.masmultimedia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import es.masmultimedia.game.SimpleSurvivorGame
import es.masmultimedia.utils.HighScoreManager

class HighScoresScreen(private val game: SimpleSurvivorGame) : Screen, InputProcessor {

    private val stage = Stage(ScreenViewport())
    private val highScoreManager = HighScoreManager()

    override fun show() {
        Gdx.input.inputProcessor = stage
        val skin = Skin(Gdx.files.internal("uiskin.json"))

        // Crear una tabla para organizar los widgets
        val table = Table()
        table.setFillParent(true)
        table.center()

        // Título
        val titleLabel = Label("Puntuaciones Altas", skin)
        titleLabel.setFontScale(2f)

        table.add(titleLabel).padBottom(40f).row()

        // Obtener las puntuaciones altas
        val highScores = highScoreManager.getHighScores()

        // Mostrar las puntuaciones altas
        for ((index, scoreEntry) in highScores.withIndex()) {
            val scoreLabel = Label("${index + 1}. ${scoreEntry.first}: ${scoreEntry.second}", skin)
            table.add(scoreLabel).padBottom(10f).row()
        }

        // Botón para volver al menú principal
        val backButton = TextButton("Volver al Menú", skin)
        backButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.screen = MainMenuScreen(game)
                dispose()
            }
        })

        table.add(backButton).width(200f).height(50f).padTop(30f)

        // Añadir la tabla al stage
        stage.addActor(table)

        // Set up the InputMultiplexer
        val inputMultiplexer = InputMultiplexer(this, stage)
        Gdx.input.inputProcessor = inputMultiplexer
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Dibujar la interfaz
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun dispose() {
        stage.dispose()
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
            // Go back to the main menu
            game.screen = MainMenuScreen(game)
            dispose()
            return true
        }
        return false
    }

    override fun keyUp(keycode: Int): Boolean = false
    override fun keyTyped(character: Char): Boolean = false
    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = false
    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean = false
    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean = false
    override fun mouseMoved(screenX: Int, screenY: Int): Boolean = false
    override fun scrolled(amountX: Float, amountY: Float): Boolean = false
    override fun touchCancelled(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

}
