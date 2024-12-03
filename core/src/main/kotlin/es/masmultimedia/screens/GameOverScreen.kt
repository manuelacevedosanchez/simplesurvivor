package es.masmultimedia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import es.masmultimedia.game.SimpleSurvivorGame

class GameOverScreen(
    private val game: SimpleSurvivorGame,
    private val score: Int,
    private val enemiesDefeated: Int,
    private val timePlayed: Long // Tiempo en milisegundos
) : Screen {

    private val stage = Stage(ScreenViewport())
    private lateinit var backgroundTexture: Texture
    private lateinit var font: BitmapFont
    private lateinit var batch: SpriteBatch

    override fun show() {
        Gdx.input.inputProcessor = stage
        val skin = Skin(Gdx.files.internal("uiskin.json"))

        batch = SpriteBatch()
        font = BitmapFont()

        // Cargar una imagen de fondo si deseas
        backgroundTexture = Texture("game_over_background.png") // Opcional

        // Crear una tabla para organizar los widgets
        val table = Table()
        table.setFillParent(true)
        table.center()

        // Título de "¡Has perdido!"
        val titleLabel = Label("¡Has perdido!", skin)
        titleLabel.setFontScale(2f)

        // Mostrar la puntuación y estadísticas
        val scoreLabel = Label("Puntuación: $score", skin)
        val enemiesLabel = Label("Enemigos eliminados: $enemiesDefeated", skin)
        val timeLabel = Label("Tiempo jugado: ${timePlayed / 1000} segundos", skin)

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
        table.add(retryButton).width(200f).height(50f).padBottom(20f).row()
        table.add(menuButton).width(200f).height(50f)

        // Añadir la tabla al stage
        stage.addActor(table)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Dibujar el fondo si tienes uno
        if (::backgroundTexture.isInitialized) {
            batch.begin()
            batch.draw(
                backgroundTexture,
                0f,
                0f,
                Gdx.graphics.width.toFloat(),
                Gdx.graphics.height.toFloat()
            )
            batch.end()
        }

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
        if (::backgroundTexture.isInitialized) {
            backgroundTexture.dispose()
        }
        batch.dispose()
        font.dispose()
    }
}
