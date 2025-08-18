package es.masmultimedia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.viewport.ScreenViewport
import es.masmultimedia.game.SimpleSurvivorGame

class MainMenuScreen(private val game: SimpleSurvivorGame) : Screen, InputProcessor {
    private val stage = Stage(ScreenViewport())
    private lateinit var backgroundTexture: Texture

    override fun show() {
        val skin = Skin(Gdx.files.internal("uiskin.json"))

        // Obtener el ancho y alto de la pantalla
        val screenWidth = Gdx.graphics.width.toFloat()
        val screenHeight = Gdx.graphics.height.toFloat()

        // Cargar una imagen de fondo (opcional)
        backgroundTexture = Texture("menu_background.png")

        // Crear una tabla para organizar los widgets
        val table = Table()
        table.setFillParent(true)
        table.center()

        // Generar una fuente personalizada desde un archivo OTF
        val fontGenerator = FreeTypeFontGenerator(Gdx.files.internal("wheaton_capitals.otf"))
        val fontParameter = FreeTypeFontGenerator.FreeTypeFontParameter()
        fontParameter.size = (screenHeight * 0.05f).toInt() // 5% del alto de la pantalla
        fontParameter.color = Color.WHITE
        val font = fontGenerator.generateFont(fontParameter)
        fontGenerator.dispose()

        // Crear estilos personalizados
        val labelStyle = Label.LabelStyle()
        labelStyle.font = font

        val textButtonStyle = TextButton.TextButtonStyle()
        textButtonStyle.up = skin.getDrawable("default-round")
        textButtonStyle.down = skin.getDrawable("default-round-down")
        textButtonStyle.font = font

        // Añadir el título del juego
        val titleLabel = Label("Simple Survivor Game", labelStyle)
        titleLabel.setFontScale(1.5f) // Ajusta según sea necesario

        // Crear los botones con el estilo personalizado
        val playButton = TextButton("Play", textButtonStyle)
        val highScoresButton = TextButton("High Scores", textButtonStyle)
        val settingsButton = TextButton("Settings", textButtonStyle)
        val infoButton = TextButton("Information", textButtonStyle)
        val exitButton = TextButton("Exit", textButtonStyle)

        // Añadir listeners a los botones
        playButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.screen = GameScreen(game)
                dispose()
            }
        })

        highScoresButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                game.screen = HighScoresScreen(game)
                dispose()
            }
        })

        exitButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                Gdx.app.exit()
            }
        })

        // Calcular tamaños relativos para los botones
        val buttonWidth = screenWidth * 0.2f // 60% del ancho de la pantalla
        val buttonHeight = screenHeight * 0.1f // 10% del alto de la pantalla

        // Añadir los widgets a la tabla
        table.add(titleLabel).padBottom(screenHeight * 0.05f)
        table.row()
        table.add(playButton).width(buttonWidth).height(buttonHeight)
            .padBottom(screenHeight * 0.02f).fillX().uniformX()
        table.row()
        table.add(highScoresButton).width(buttonWidth).height(buttonHeight)
            .padBottom(screenHeight * 0.02f).fillX().uniformX()
        table.row()
        table.add(settingsButton).width(buttonWidth).height(buttonHeight)
            .padBottom(screenHeight * 0.02f).fillX().uniformX()
        table.row()
        table.add(infoButton).width(buttonWidth).height(buttonHeight)
            .padBottom(screenHeight * 0.02f).fillX().uniformX()
        table.row()
        table.add(exitButton).width(buttonWidth).height(buttonHeight).fillX().uniformX()

        // Añadir la tabla al stage
        stage.addActor(table)

        // Crear el InputMultiplexer
        val inputMultiplexer = InputMultiplexer(this, stage)
        Gdx.input.inputProcessor = inputMultiplexer
    }

    override fun render(delta: Float) {
        // Dibujar el fondo
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.batch.begin()
        stage.batch.draw(
            backgroundTexture,
            0f,
            0f,
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat()
        )
        stage.batch.end()

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
        backgroundTexture.dispose()
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
            // Mostrar diálogo de confirmación para salir
            val dialog = object : Dialog("Salir", Skin(Gdx.files.internal("uiskin.json"))) {
                override fun result(result: Any?) {
                    if (result == null) return
                    if (result as Boolean) {
                        Gdx.app.exit()
                    } else {
                        hide()
                    }
                }
            }
            dialog.text("¿Deseas salir del juego?")
            dialog.button("Sí", true)
            dialog.button("No", false)
            dialog.show(stage)
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
