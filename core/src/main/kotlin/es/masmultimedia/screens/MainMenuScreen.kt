package es.masmultimedia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import es.masmultimedia.game.SimpleSurvivorGame

class MainMenuScreen(private val game: SimpleSurvivorGame) : Screen {
    private val stage = Stage(ScreenViewport())
    private val camera = OrthographicCamera().apply {
        setToOrtho(false, 800f, 600f)
    }

    override fun show() {
        Gdx.input.inputProcessor = stage
        val skin = Skin(Gdx.files.internal("uiskin.json"))

        val playButton = TextButton("Play", skin).apply {
            setPosition(300f, 400f)
            setSize(200f, 50f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    game.screen = GameScreen(game)
                    dispose()
                }
            })
        }

        val highScoresButton = TextButton("High Scores", skin).apply {
            setPosition(300f, 340f)
            setSize(200f, 50f)
        }

        val settingsButton = TextButton("Settings", skin).apply {
            setPosition(300f, 280f)
            setSize(200f, 50f)
        }

        val infoButton = TextButton("Information", skin).apply {
            setPosition(300f, 220f)
            setSize(200f, 50f)
        }

        val exitButton = TextButton("Exit", skin).apply {
            setPosition(300f, 160f)
            setSize(200f, 50f)
            addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    Gdx.app.exit()
                }
            })
        }

        stage.addActor(playButton)
        stage.addActor(highScoresButton)
        stage.addActor(settingsButton)
        stage.addActor(infoButton)
        stage.addActor(exitButton)
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

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
