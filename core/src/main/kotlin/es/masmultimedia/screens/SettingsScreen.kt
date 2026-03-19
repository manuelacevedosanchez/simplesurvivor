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
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.InputEvent
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Slider
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.TextButton
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.viewport.ScreenViewport
import es.masmultimedia.game.SimpleSurvivorGame
import es.masmultimedia.utils.AudioManager
import es.masmultimedia.utils.GameSettings
import es.masmultimedia.utils.HighScoreManager
import es.masmultimedia.utils.I18n
import java.util.Locale

class SettingsScreen(private val game: SimpleSurvivorGame) : Screen, InputProcessor {

    private val stage = Stage(ScreenViewport())
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
        val sectionFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (screenHeight * 0.038f).toInt().coerceAtLeast(17)
            color = Color.GOLD
        })
        val bodyFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (screenHeight * 0.03f).toInt().coerceAtLeast(14)
            color = Color.WHITE
        })
        generator.dispose()

        val titleStyle = Label.LabelStyle(titleFont, Color.WHITE)
        val sectionStyle = Label.LabelStyle(sectionFont, Color.GOLD)
        val bodyStyle = Label.LabelStyle(bodyFont, Color.WHITE)
        val valueStyle = Label.LabelStyle(bodyFont, Color.GOLD)
        val dangerStyle = Label.LabelStyle(bodyFont, Color.RED)

        val buttonStyle = TextButton.TextButtonStyle().apply {
            up = skin.getDrawable("default-round")
            down = skin.getDrawable("default-round-down")
            font = bodyFont
        }

        val dangerButtonStyle = TextButton.TextButtonStyle().apply {
            up = skin.getDrawable("default-round")
            down = skin.getDrawable("default-round-down")
            font = bodyFont
            fontColor = Color.RED
        }

        // Scale checkbox icons to match font size
        val checkSize = bodyFont.lineHeight * 1.2f
        val checkOnDrawable = skin.getDrawable("check-on")
        checkOnDrawable.minWidth = checkSize
        checkOnDrawable.minHeight = checkSize
        val checkOffDrawable = skin.getDrawable("check-off")
        checkOffDrawable.minWidth = checkSize
        checkOffDrawable.minHeight = checkSize

        val checkBoxStyle = CheckBox.CheckBoxStyle().apply {
            checkboxOn = checkOnDrawable
            checkboxOff = checkOffDrawable
            font = bodyFont
            fontColor = Color.WHITE
        }

        val sectionPad = screenHeight * 0.03f
        val itemPad = screenHeight * 0.015f
        val sliderWidth = screenWidth * 0.55f
        val sliderHeight = screenHeight * 0.05f

        // --- Content table (scrollable) ---
        val contentTable = Table()
        contentTable.top()
        contentTable.pad(screenHeight * 0.02f, screenWidth * 0.08f, screenHeight * 0.02f, screenWidth * 0.08f)

        // ============================
        // ===== SOUND =====
        // ============================
        contentTable.add(Label(I18n.get("sound"), sectionStyle)).left().colspan(2)
            .padTop(sectionPad).padBottom(itemPad).row()

        // Sound enabled toggle
        val soundCheckBox = CheckBox(" " + I18n.get("on"), checkBoxStyle)
        soundCheckBox.isChecked = AudioManager.soundEnabled
        soundCheckBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                AudioManager.soundEnabled = soundCheckBox.isChecked
                if (soundCheckBox.isChecked) AudioManager.playButtonClick()
            }
        })
        contentTable.add(soundCheckBox).left().colspan(2).padBottom(itemPad).row()

        // Sound volume
        val soundVolValue = Label("${(AudioManager.soundVolume * 100).toInt()}%", valueStyle)
        soundVolValue.setAlignment(Align.right)
        val soundSlider = Slider(0f, 1f, 0.05f, false, skin)
        soundSlider.value = AudioManager.soundVolume
        soundSlider.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                AudioManager.soundVolume = soundSlider.value
                soundVolValue.setText("${(soundSlider.value * 100).toInt()}%")
            }
        })
        contentTable.add(Label(I18n.get("sound_volume"), bodyStyle)).left().padRight(itemPad)
        contentTable.add(soundVolValue).right().row()
        contentTable.add(soundSlider).width(sliderWidth).height(sliderHeight).colspan(2).left()
            .padBottom(itemPad).row()

        // ============================
        // ===== MUSIC =====
        // ============================
        contentTable.add(Label(I18n.get("music"), sectionStyle)).left().colspan(2)
            .padTop(sectionPad).padBottom(itemPad).row()

        // Music enabled toggle
        val musicCheckBox = CheckBox(" " + I18n.get("on"), checkBoxStyle)
        musicCheckBox.isChecked = AudioManager.musicEnabled
        musicCheckBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                AudioManager.musicEnabled = musicCheckBox.isChecked
                if (musicCheckBox.isChecked) AudioManager.playMenuMusic()
            }
        })
        contentTable.add(musicCheckBox).left().colspan(2).padBottom(itemPad).row()

        // Music volume
        val musicVolValue = Label("${(AudioManager.musicVolume * 100).toInt()}%", valueStyle)
        musicVolValue.setAlignment(Align.right)
        val musicSlider = Slider(0f, 1f, 0.05f, false, skin)
        musicSlider.value = AudioManager.musicVolume
        musicSlider.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                AudioManager.musicVolume = musicSlider.value
                musicVolValue.setText("${(musicSlider.value * 100).toInt()}%")
            }
        })
        contentTable.add(Label(I18n.get("music_volume"), bodyStyle)).left().padRight(itemPad)
        contentTable.add(musicVolValue).right().row()
        contentTable.add(musicSlider).width(sliderWidth).height(sliderHeight).colspan(2).left()
            .padBottom(itemPad).row()

        // ============================
        // ===== CONTROLS =====
        // ============================
        contentTable.add(Label(I18n.get("controls"), sectionStyle)).left().colspan(2)
            .padTop(sectionPad).padBottom(itemPad).row()

        // Joystick size
        val joystickValue = Label("${(GameSettings.joystickSize * 100).toInt()}%", valueStyle)
        joystickValue.setAlignment(Align.right)
        val joystickSlider = Slider(0.5f, 1.5f, 0.05f, false, skin)
        joystickSlider.value = GameSettings.joystickSize
        joystickSlider.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                GameSettings.joystickSize = joystickSlider.value
                joystickValue.setText("${(joystickSlider.value * 100).toInt()}%")
            }
        })
        contentTable.add(Label(I18n.get("joystick_size"), bodyStyle)).left().padRight(itemPad)
        contentTable.add(joystickValue).right().row()
        contentTable.add(joystickSlider).width(sliderWidth).height(sliderHeight).colspan(2).left()
            .padBottom(itemPad).row()

        // Vibration toggle
        val vibrationCheckBox = CheckBox(" " + I18n.get("vibration"), checkBoxStyle)
        vibrationCheckBox.isChecked = GameSettings.vibrationEnabled
        vibrationCheckBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                GameSettings.vibrationEnabled = vibrationCheckBox.isChecked
                if (vibrationCheckBox.isChecked) GameSettings.vibrate(50)
            }
        })
        contentTable.add(vibrationCheckBox).left().colspan(2).padBottom(itemPad).row()

        // ============================
        // ===== DISPLAY =====
        // ============================
        contentTable.add(Label(I18n.get("display"), sectionStyle)).left().colspan(2)
            .padTop(sectionPad).padBottom(itemPad).row()

        // Show FPS toggle
        val fpsCheckBox = CheckBox(" " + I18n.get("show_fps"), checkBoxStyle)
        fpsCheckBox.isChecked = GameSettings.showFps
        fpsCheckBox.addListener(object : ChangeListener() {
            override fun changed(event: ChangeEvent?, actor: Actor?) {
                GameSettings.showFps = fpsCheckBox.isChecked
            }
        })
        contentTable.add(fpsCheckBox).left().colspan(2).padBottom(itemPad).row()

        // ============================
        // ===== LANGUAGE =====
        // ============================
        contentTable.add(Label(I18n.get("language"), sectionStyle)).left().colspan(2)
            .padTop(sectionPad).padBottom(itemPad).row()

        val languages = listOf(
            Triple("lang_en", "en", Locale.ENGLISH),
            Triple("lang_es", "es", Locale("es")),
            Triple("lang_pt", "pt", Locale("pt"))
        )

        val langButtonWidth = screenWidth * 0.55f
        val langButtonHeight = screenHeight * 0.07f
        val currentLang = I18n.getCurrentLanguage()

        for ((i18nKey, langCode, locale) in languages) {
            val isCurrentLang = currentLang == langCode
            val langBtnStyle = TextButton.TextButtonStyle().apply {
                up = if (isCurrentLang) skin.getDrawable("default-round-down") else skin.getDrawable("default-round")
                down = skin.getDrawable("default-round-down")
                font = bodyFont
                fontColor = if (isCurrentLang) Color.GOLD else Color.WHITE
            }
            val langText = if (isCurrentLang) "▸ ${I18n.get(i18nKey)} ◂" else I18n.get(i18nKey)
            val langButton = TextButton(langText, langBtnStyle)
            langButton.addListener(object : ClickListener() {
                override fun clicked(event: InputEvent?, x: Float, y: Float) {
                    AudioManager.playButtonClick()
                    I18n.setLanguage(locale)
                    game.screen = SettingsScreen(game)
                    dispose()
                }
            })
            contentTable.add(langButton).width(langButtonWidth).height(langButtonHeight)
                .colspan(2).left().padBottom(itemPad).row()
        }

        // ============================
        // ===== DATA =====
        // ============================
        contentTable.add(Label(I18n.get("data"), sectionStyle)).left().colspan(2)
            .padTop(sectionPad).padBottom(itemPad).row()

        // Clear high scores button
        val clearButton = TextButton(I18n.get("clear_high_scores"), dangerButtonStyle)
        clearButton.addListener(object : ClickListener() {
            override fun clicked(event: InputEvent?, x: Float, y: Float) {
                AudioManager.playButtonClick()
                showClearScoresDialog(skin, bodyFont)
            }
        })
        contentTable.add(clearButton).width(langButtonWidth).height(langButtonHeight)
            .colspan(2).left().padBottom(sectionPad).row()

        // --- ScrollPane ---
        val scrollPane = ScrollPane(contentTable, skin)
        scrollPane.setFadeScrollBars(false)
        scrollPane.setScrollingDisabled(true, false)

        // --- Root table ---
        val rootTable = Table()
        rootTable.setFillParent(true)
        rootTable.pad(screenHeight * 0.06f, screenWidth * 0.05f, screenHeight * 0.03f, screenWidth * 0.05f)

        // Screen title
        val screenTitle = Label(I18n.get("settings"), titleStyle)
        screenTitle.setAlignment(Align.center)
        rootTable.add(screenTitle).expandX().fillX().padBottom(screenHeight * 0.03f).row()

        // Scroll area
        rootTable.add(scrollPane).expand().fill().row()

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

        stage.addActor(rootTable)

        val inputMultiplexer = InputMultiplexer(this, stage)
        Gdx.input.inputProcessor = inputMultiplexer
    }

    private fun showClearScoresDialog(skin: Skin, bodyFont: com.badlogic.gdx.graphics.g2d.BitmapFont) {
        val dialogBodyStyle = Label.LabelStyle(bodyFont, Color.WHITE)
        val dialogButtonStyle = TextButton.TextButtonStyle().apply {
            up = skin.getDrawable("default-round")
            down = skin.getDrawable("default-round-down")
            font = bodyFont
        }

        val screenW = Gdx.graphics.width.toFloat()
        val screenH = Gdx.graphics.height.toFloat()
        val btnWidth = screenW * 0.25f
        val btnHeight = screenH * 0.07f
        val pad = screenH * 0.02f

        val dialog = object : Dialog("", skin) {
            override fun result(result: Any?) {
                if (result == null) return
                if (result as Boolean) {
                    HighScoreManager().clearHighScores()
                    showConfirmationDialog(skin, bodyFont)
                }
            }
        }

        dialog.contentTable.add(Label(I18n.get("clear_scores_confirm"), dialogBodyStyle)).pad(pad).row()
        dialog.buttonTable.defaults().width(btnWidth).height(btnHeight).pad(pad * 0.4f)
        dialog.button(TextButton(I18n.get("yes"), dialogButtonStyle), true)
        dialog.button(TextButton(I18n.get("no"), dialogButtonStyle), false)
        dialog.show(stage)
    }

    private fun showConfirmationDialog(skin: Skin, bodyFont: com.badlogic.gdx.graphics.g2d.BitmapFont) {
        val dialogBodyStyle = Label.LabelStyle(bodyFont, Color.WHITE)
        val dialog = Dialog("", skin)
        dialog.contentTable.add(Label(I18n.get("clear_scores_done"), dialogBodyStyle)).pad(Gdx.graphics.height * 0.02f)
        dialog.button("OK")
        dialog.show(stage)
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
