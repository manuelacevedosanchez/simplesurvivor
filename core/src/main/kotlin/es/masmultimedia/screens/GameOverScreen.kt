package es.masmultimedia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.ui.TextField
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import es.masmultimedia.game.SimpleSurvivorGame
import es.masmultimedia.utils.HighScoreManager

class GameOverScreen(
    private val game: SimpleSurvivorGame,
    private val score: Int,
    private val enemiesDefeated: Int,
    private val timePlayed: Long, // Time played in milliseconds
    private val endMessage: String = "¡Juego Terminado!"
) : Screen, InputProcessor {

    private val stage = Stage(ScreenViewport())
    private val highScoreManager = HighScoreManager()

    override fun show() {
        val skin = Skin(Gdx.files.internal("uiskin.json"))

        // Build a table layout for the widgets.
        val table = Table()
        table.setFillParent(true)
        table.center()

        // End title (loss/win/custom message).
        val titleLabel = Label(endMessage, skin)
        titleLabel.setFontScale(2f)

        // Display score and run stats.
        val scoreLabel = Label("Puntuación: $score", skin)
        val enemiesLabel = Label("Enemigos eliminados: $enemiesDefeated", skin)
        val timeLabel = Label("Tiempo jugado: ${timePlayed / 1000} segundos", skin)

        // Check whether this score qualifies as a high score.
        val isHighScore = checkIfHighScore(score)

        // Name input shown only for high-score entries.
        val nameLabel = Label("Introduce tu nombre:", skin)
        val nameTextField = TextField("", skin)
        val saveButton = TextButton("Guardar Puntuación", skin)

        saveButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                val playerName = nameTextField.text
                if (playerName.isNotEmpty()) {
                    highScoreManager.addHighScore(playerName, score)
                    showSavedDialog()
                }
            }
        })

        // Create action buttons.
        val retryButton = TextButton("Volver a jugar", skin)
        val menuButton = TextButton("Menú principal", skin)

        // Attach button listeners.
        retryButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.screen = GameScreen(game)
                dispose()
            }
        })

        menuButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.screen = MainMenuScreen(game)
                dispose()
            }
        })

        // Add widgets to the table.
        table.add(titleLabel).padBottom(40f).row()
        table.add(scoreLabel).padBottom(20f).row()
        table.add(enemiesLabel).padBottom(20f).row()
        table.add(timeLabel).padBottom(40f).row()

        if (isHighScore) {
            table.add(nameLabel).padBottom(10f).row()
            table.add(nameTextField).width(200f).padBottom(20f).row()
            table.add(saveButton).width(200f).height(50f).padBottom(20f).row()
        }

        table.add(retryButton).width(200f).height(50f).padBottom(20f).row()
        table.add(menuButton).width(200f).height(50f)

        // Add table to stage.
        stage.addActor(table)

        // Route inputs to both this screen and the stage.
        val inputMultiplexer = InputMultiplexer(this, stage)
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private fun checkIfHighScore(score: Int): Boolean {
        val highScores = highScoreManager.getHighScores()
        if (highScores.size < 10) {
            return true
        }
        val lowestHighScore = highScores.minByOrNull { it.second }?.second ?: 0
        return score > lowestHighScore
    }

    private fun showSavedDialog() {
        val dialog = Dialog("Puntuación Guardada", Skin(Gdx.files.internal("uiskin.json")))
        dialog.text("¡Tu puntuación ha sido guardada!")
        dialog.button("OK")
        dialog.show(stage)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Draw UI.
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
            // Return to the main menu.
            game.screen = MainMenuScreen(game)
            dispose()
            return true // Event handled.
        }
        return false // Let other processors handle it if needed.
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
