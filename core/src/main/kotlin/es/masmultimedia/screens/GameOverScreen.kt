package es.masmultimedia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
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
import es.masmultimedia.utils.AudioManager
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
        // Play game over music
        AudioManager.playGameOverMusic()

        val skin = Skin(Gdx.files.internal("uiskin.json"))

        // Build density-aware fonts so the screen is readable on any phone.
        val generator = FreeTypeFontGenerator(Gdx.files.internal("wheaton_capitals.otf"))
        val titleFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size  = (Gdx.graphics.height * 0.07f).toInt().coerceAtLeast(28)
            color = Color.WHITE
        })
        val bodyFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size  = (Gdx.graphics.height * 0.045f).toInt().coerceAtLeast(18)
            color = Color.WHITE
        })
        generator.dispose()

        val titleStyle  = Label.LabelStyle(titleFont, Color.WHITE)
        val bodyStyle   = Label.LabelStyle(bodyFont,  Color.WHITE)
        val buttonStyle = TextButton.TextButtonStyle().apply {
            up   = skin.getDrawable("default-round")
            down = skin.getDrawable("default-round-down")
            font = bodyFont
        }
        val fieldStyle = skin.get(TextField.TextFieldStyle::class.java).also {
            it.font = bodyFont
        }

        val btnWidth  = Gdx.graphics.width  * 0.55f
        val btnHeight = Gdx.graphics.height * 0.095f
        val padBottom = Gdx.graphics.height * 0.025f

        // Build a table layout for the widgets.
        val table = Table()
        table.setFillParent(true)
        table.center()

        // End title (loss/win/custom message).
        val titleLabel = Label(endMessage, titleStyle)

        // Display score and run stats.
        val scoreLabel   = Label("Puntuación: $score", bodyStyle)
        val enemiesLabel = Label("Enemigos eliminados: $enemiesDefeated", bodyStyle)
        val timeLabel    = Label("Tiempo jugado: ${timePlayed / 1000} segundos", bodyStyle)

        // Check whether this score qualifies as a high score.
        val isHighScore = checkIfHighScore(score)

        // Name input shown only for high-score entries.
        val nameLabel     = Label("Introduce tu nombre:", bodyStyle)
        val nameTextField = TextField("", skin).also { it.style = fieldStyle }
        val saveButton    = TextButton("Guardar Puntuación", skin).also { it.style = buttonStyle }

        saveButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                val playerName = nameTextField.text
                if (playerName.isNotEmpty()) {
                    highScoreManager.addHighScore(playerName, score)
                    showSavedDialog(skin, bodyFont)
                }
            }
        })

        // Create action buttons.
        val retryButton = TextButton("Volver a jugar", skin).also { it.style = buttonStyle }
        val menuButton  = TextButton("Menú principal",  skin).also { it.style = buttonStyle }

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
        table.add(titleLabel).padBottom(padBottom * 1.5f).row()
        table.add(scoreLabel).padBottom(padBottom).row()
        table.add(enemiesLabel).padBottom(padBottom).row()
        table.add(timeLabel).padBottom(padBottom * 1.5f).row()

        if (isHighScore) {
            table.add(nameLabel).padBottom(padBottom * 0.5f).row()
            table.add(nameTextField).width(btnWidth).padBottom(padBottom).row()
            table.add(saveButton).size(btnWidth, btnHeight).padBottom(padBottom).row()
        }

        table.add(retryButton).size(btnWidth, btnHeight).padBottom(padBottom).row()
        table.add(menuButton).size(btnWidth, btnHeight)

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

    private fun showSavedDialog(skin: Skin, bodyFont: com.badlogic.gdx.graphics.g2d.BitmapFont) {
        val dialog = Dialog("Puntuación Guardada", skin)
        val bodyStyle = Label.LabelStyle(bodyFont, com.badlogic.gdx.graphics.Color.WHITE)
        dialog.contentTable.add(Label("¡Tu puntuación ha sido guardada!", bodyStyle)).pad(20f)
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
