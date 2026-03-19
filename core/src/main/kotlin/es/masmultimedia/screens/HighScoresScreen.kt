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
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import es.masmultimedia.game.SimpleSurvivorGame
import es.masmultimedia.utils.AudioManager
import es.masmultimedia.utils.HighScoreManager
import es.masmultimedia.utils.I18n

class HighScoresScreen(private val game: SimpleSurvivorGame) : Screen, InputProcessor {

    private val stage = Stage(ScreenViewport())
    private val highScoreManager = HighScoreManager()
    private lateinit var backgroundTexture: Texture

    override fun show() {
        val skin = Skin(Gdx.files.internal("uiskin.json"))

        val screenWidth = Gdx.graphics.width.toFloat()
        val screenHeight = Gdx.graphics.height.toFloat()

        backgroundTexture = Texture("menu_background.png")

        // --- Density-aware fonts ---
        val generator = FreeTypeFontGenerator(Gdx.files.internal("wheaton_capitals.otf"))

        val titleFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (screenHeight * 0.055f).toInt().coerceAtLeast(24)
            color = Color.WHITE
        })
        val headerFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (screenHeight * 0.03f).toInt().coerceAtLeast(14)
            color = Color.GOLD
        })
        val bodyFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (screenHeight * 0.03f).toInt().coerceAtLeast(14)
            color = Color.WHITE
        })
        val rankFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (screenHeight * 0.033f).toInt().coerceAtLeast(15)
            color = Color.GOLD
        })
        generator.dispose()

        val titleStyle = Label.LabelStyle(titleFont, Color.WHITE)
        val headerStyle = Label.LabelStyle(headerFont, Color.GOLD)
        val bodyStyle = Label.LabelStyle(bodyFont, Color.WHITE)
        val rankStyle = Label.LabelStyle(rankFont, Color.GOLD)
        val emptyStyle = Label.LabelStyle(bodyFont, Color.GRAY)

        val buttonStyle = TextButton.TextButtonStyle().apply {
            up = skin.getDrawable("default-round")
            down = skin.getDrawable("default-round-down")
            font = bodyFont
        }

        val rowPad = screenHeight * 0.012f
        val colPad = screenWidth * 0.03f

        // --- Root table ---
        val rootTable = Table()
        rootTable.setFillParent(true)
        rootTable.pad(screenHeight * 0.06f, screenWidth * 0.05f, screenHeight * 0.03f, screenWidth * 0.05f)

        // Screen title
        val screenTitle = Label(I18n.get("high_scores"), titleStyle)
        screenTitle.setAlignment(Align.center)
        rootTable.add(screenTitle)
            .expandX().fillX()
            .padBottom(screenHeight * 0.04f)
            .row()

        // --- Scores content table ---
        val scoresTable = Table()
        scoresTable.top().left()
        scoresTable.pad(rowPad)

        val highScores = highScoreManager.getHighScores()

        if (highScores.isEmpty()) {
            val emptyLabel = Label(I18n.get("high_scores_empty"), emptyStyle)
            emptyLabel.setAlignment(Align.center)
            emptyLabel.wrap = true
            scoresTable.add(emptyLabel).expandX().fillX().padTop(screenHeight * 0.1f).row()
        } else {
            // Table header
            val headerRank = Label("#", headerStyle)
            headerRank.setAlignment(Align.center)
            val headerName = Label(I18n.get("high_scores_name"), headerStyle)
            headerName.setAlignment(Align.left)
            val headerScore = Label(I18n.get("high_scores_score"), headerStyle)
            headerScore.setAlignment(Align.right)
            val headerTime = Label(I18n.get("time"), headerStyle)
            headerTime.setAlignment(Align.right)

            scoresTable.add(headerRank).padRight(colPad).padBottom(rowPad)
            scoresTable.add(headerName).expandX().fillX().padRight(colPad).padBottom(rowPad)
            scoresTable.add(headerScore).padRight(colPad).padBottom(rowPad)
            scoresTable.add(headerTime).padBottom(rowPad)
            scoresTable.row()

            // Score entries
            for ((index, scoreEntry) in highScores.withIndex()) {
                val position = index + 1

                val posStyle = when (position) {
                    1 -> Label.LabelStyle(rankFont, Color.GOLD)
                    2 -> Label.LabelStyle(rankFont, Color.LIGHT_GRAY)
                    3 -> Label.LabelStyle(rankFont, Color(0.80f, 0.50f, 0.20f, 1f))
                    else -> rankStyle
                }

                val rankLabel = Label("$position", posStyle)
                rankLabel.setAlignment(Align.center)

                val nameLabel = Label(scoreEntry.first, bodyStyle)
                nameLabel.setAlignment(Align.left)

                val scoreLabel = Label("${scoreEntry.second}", bodyStyle)
                scoreLabel.setAlignment(Align.right)

                val timeLabel = Label(formatTime(scoreEntry.third), bodyStyle)
                timeLabel.setAlignment(Align.right)

                scoresTable.add(rankLabel).padRight(colPad).padBottom(rowPad)
                scoresTable.add(nameLabel).expandX().fillX().padRight(colPad).padBottom(rowPad)
                scoresTable.add(scoreLabel).padRight(colPad).padBottom(rowPad)
                scoresTable.add(timeLabel).padBottom(rowPad)
                scoresTable.row()
            }
        }

        // ScrollPane for scores
        val scrollPane = ScrollPane(scoresTable, skin)
        scrollPane.setFadeScrollBars(false)
        scrollPane.setScrollingDisabled(true, false)

        rootTable.add(scrollPane)
            .expand().fill()
            .row()

        // Back button
        val backButton = TextButton(I18n.get("back"), buttonStyle)
        backButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                AudioManager.playButtonClick()
                game.screen = MainMenuScreen(game)
                dispose()
            }
        })
        rootTable.add(backButton)
            .width(screenWidth * 0.35f)
            .height(screenHeight * 0.08f)
            .padTop(screenHeight * 0.02f)

        stage.addActor(rootTable)

        val inputMultiplexer = InputMultiplexer(this, stage)
        Gdx.input.inputProcessor = inputMultiplexer
    }

    override fun render(delta: Float) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        stage.batch.begin()
        stage.batch.draw(
            backgroundTexture,
            0f, 0f,
            Gdx.graphics.width.toFloat(),
            Gdx.graphics.height.toFloat()
        )
        stage.batch.end()

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

    private fun formatTime(timeMs: Long): String {
        if (timeMs <= 0L) return "--:--"
        val totalSeconds = (timeMs / 1000).toInt()
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes, seconds)
        } else {
            "%d:%02d".format(minutes, seconds)
        }
    }
}
