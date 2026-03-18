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
import es.masmultimedia.utils.I18n

class InformationScreen(private val game: SimpleSurvivorGame) : Screen, InputProcessor {

    private val stage = Stage(ScreenViewport())
    private lateinit var backgroundTexture: Texture

    override fun show() {
        val skin = Skin(Gdx.files.internal("uiskin.json"))

        val screenWidth = Gdx.graphics.width.toFloat()
        val screenHeight = Gdx.graphics.height.toFloat()

        backgroundTexture = Texture("menu_background.png")

        // --- Fonts ---
        val generator = FreeTypeFontGenerator(Gdx.files.internal("wheaton_capitals.otf"))

        val titleFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (screenHeight * 0.055f).toInt().coerceAtLeast(24)
            color = Color.WHITE
        })
        val sectionFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (screenHeight * 0.04f).toInt().coerceAtLeast(18)
            color = Color.GOLD
        })
        val bodyFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (screenHeight * 0.03f).toInt().coerceAtLeast(14)
            color = Color.WHITE
        })
        val smallFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (screenHeight * 0.025f).toInt().coerceAtLeast(12)
            color = Color.LIGHT_GRAY
        })
        generator.dispose()

        val titleStyle = Label.LabelStyle(titleFont, Color.WHITE)
        val sectionStyle = Label.LabelStyle(sectionFont, Color.GOLD)
        val bodyStyle = Label.LabelStyle(bodyFont, Color.WHITE)
        val smallStyle = Label.LabelStyle(smallFont, Color.LIGHT_GRAY)
        val linkStyle = Label.LabelStyle(bodyFont, Color.SKY)

        val buttonStyle = TextButton.TextButtonStyle().apply {
            up = skin.getDrawable("default-round")
            down = skin.getDrawable("default-round-down")
            font = bodyFont
        }

        val contentWidth = screenWidth * 0.85f
        val sectionPad = screenHeight * 0.03f
        val itemPad = screenHeight * 0.008f

        // --- Scrollable content table ---
        val contentTable = Table()
        contentTable.defaults().width(contentWidth).padBottom(itemPad)
        contentTable.top()

        // ===== ABOUT =====
        addSectionTitle(contentTable, I18n.get("info_about_title"), sectionStyle, sectionPad)
        addWrappedLabel(contentTable, I18n.get("info_about_desc"), bodyStyle, contentWidth)

        // ===== CONTROLS =====
        addSectionTitle(contentTable, I18n.get("info_controls_title"), sectionStyle, sectionPad)
        addWrappedLabel(contentTable, I18n.get("info_controls_move"), bodyStyle, contentWidth)
        addWrappedLabel(contentTable, I18n.get("info_controls_aim"), bodyStyle, contentWidth)

        // ===== ENEMIES =====
        addSectionTitle(contentTable, I18n.get("info_enemies_title"), sectionStyle, sectionPad)
        addWrappedLabel(contentTable, I18n.get("info_enemy_normal"), bodyStyle, contentWidth)
        addWrappedLabel(contentTable, I18n.get("info_enemy_fast"), bodyStyle, contentWidth)
        addWrappedLabel(contentTable, I18n.get("info_enemy_strong"), bodyStyle, contentWidth)

        // ===== POWER-UPS =====
        addSectionTitle(contentTable, I18n.get("info_powerups_title"), sectionStyle, sectionPad)
        addWrappedLabel(contentTable, I18n.get("info_powerups_offensive"), bodyStyle, contentWidth)
        addWrappedLabel(contentTable, I18n.get("info_powerups_defensive"), bodyStyle, contentWidth)
        addWrappedLabel(contentTable, I18n.get("info_powerups_utility"), bodyStyle, contentWidth)
        addWrappedLabel(contentTable, I18n.get("info_powerups_special"), bodyStyle, contentWidth)

        // ===== CREDITS =====
        addSectionTitle(contentTable, I18n.get("info_credits_title"), sectionStyle, sectionPad)

        // Music credits
        addWrappedLabel(contentTable, "-- ${I18n.get("info_credits_music")} --", bodyStyle, contentWidth)
        addWrappedLabel(contentTable, "\"Hitman\" - Kevin MacLeod (incompetech.com)", smallStyle, contentWidth)
        addWrappedLabel(contentTable, "  Licensed under CC BY 4.0", smallStyle, contentWidth)
        addWrappedLabel(contentTable, "emanresU (ko-fi.com/emanresu102396)", smallStyle, contentWidth)
        addWrappedLabel(contentTable, "Devlin Bataric", smallStyle, contentWidth)

        // SFX credits
        addWrappedLabel(contentTable, "-- ${I18n.get("info_credits_sfx")} --", bodyStyle, contentWidth)
        addWrappedLabel(contentTable, "Impact Sounds (1.0) - Kenney (kenney.nl)", smallStyle, contentWidth)
        addWrappedLabel(contentTable, "  Licensed under CC0 1.0", smallStyle, contentWidth)

        // ===== DEVELOPER =====
        contentTable.row()
        val devLabel = Label("${I18n.get("info_credits_developed")}:", sectionStyle)
        contentTable.add(devLabel).padTop(sectionPad).padBottom(itemPad).row()

        val devNameLabel = Label(I18n.get("info_developer_name"), bodyStyle)
        contentTable.add(devNameLabel).padBottom(itemPad).row()

        // LinkedIn button
        val linkedinButton = TextButton(I18n.get("info_visit_linkedin"), buttonStyle)
        linkedinButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                AudioManager.playButtonClick()
                Gdx.net.openURI("https://www.linkedin.com/in/manuelacevedosanchez/")
            }
        })
        contentTable.add(linkedinButton)
            .width(contentWidth * 0.6f)
            .height(screenHeight * 0.07f)
            .padTop(itemPad)
            .padBottom(sectionPad)
            .row()

        // --- ScrollPane ---
        val scrollPane = ScrollPane(contentTable, skin)
        scrollPane.setFadeScrollBars(false)
        scrollPane.setScrollingDisabled(true, false)

        // --- Root table ---
        val rootTable = Table()
        rootTable.setFillParent(true)

        // Screen title
        val screenTitle = Label(I18n.get("info"), titleStyle)
        rootTable.add(screenTitle).padTop(screenHeight * 0.03f).padBottom(screenHeight * 0.02f).row()

        // Scroll area
        rootTable.add(scrollPane)
            .expand()
            .fill()
            .pad(0f, screenWidth * 0.05f, 0f, screenWidth * 0.05f)
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
            .width(screenWidth * 0.3f)
            .height(screenHeight * 0.08f)
            .padTop(screenHeight * 0.02f)
            .padBottom(screenHeight * 0.03f)

        stage.addActor(rootTable)

        val inputMultiplexer = InputMultiplexer(this, stage)
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private fun addSectionTitle(table: Table, text: String, style: Label.LabelStyle, topPad: Float) {
        table.row()
        val label = Label(text, style)
        table.add(label).padTop(topPad).padBottom(topPad * 0.3f).row()
    }

    private fun addWrappedLabel(table: Table, text: String, style: Label.LabelStyle, width: Float) {
        val label = Label(text, style)
        label.wrap = true
        label.setAlignment(Align.left)
        table.add(label).width(width).row()
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
}

