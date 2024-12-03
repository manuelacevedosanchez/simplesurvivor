package es.masmultimedia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.*
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import es.masmultimedia.game.SimpleSurvivorGame
import es.masmultimedia.utils.HighScoreManager

class GameOverScreen(
    private val game: SimpleSurvivorGame,
    private val score: Int,
    private val enemiesDefeated: Int,
    private val timePlayed: Long // Tiempo en milisegundos
) : Screen {

    private val stage = Stage(ScreenViewport())
    private lateinit var backgroundTexture: Texture
    private val highScoreManager = HighScoreManager()

    override fun show() {
        Gdx.input.inputProcessor = stage
        val skin = Skin(Gdx.files.internal("uiskin.json"))

        // Crear una tabla para organizar los widgets
        val table = Table()
        table.setFillParent(true)
        table.center()

        // Título de "¡Has perdido!" o "¡Has ganado!" dependiendo del resultado
        val titleLabel = Label("¡Juego Terminado!", skin)
        titleLabel.setFontScale(2f)

        // Mostrar la puntuación y estadísticas
        val scoreLabel = Label("Puntuación: $score", skin)
        val enemiesLabel = Label("Enemigos eliminados: $enemiesDefeated", skin)
        val timeLabel = Label("Tiempo jugado: ${timePlayed / 1000} segundos", skin)

        // Verificar si la puntuación es una puntuación alta
        val isHighScore = checkIfHighScore(score)

        // Campos para introducir el nombre si es una puntuación alta
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

        // Crear botones
        val retryButton = TextButton("Volver a jugar", skin)
        val menuButton = TextButton("Menú principal", skin)

        // Añadir listeners a los botones
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

        // Añadir widgets a la tabla
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

        // Añadir la tabla al stage
        stage.addActor(table)
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
}
