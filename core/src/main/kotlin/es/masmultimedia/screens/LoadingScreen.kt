package es.masmultimedia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.utils.viewport.ScreenViewport
import com.badlogic.gdx.utils.viewport.Viewport
import es.masmultimedia.game.SimpleSurvivorGame
import es.masmultimedia.utils.GameAssetManager

/**
 * Loading screen that displays progress while assets are loaded asynchronously.
 * This prevents ANR (Application Not Responding) errors on Android devices.
 */
class LoadingScreen(private val game: SimpleSurvivorGame) : Screen {

    private lateinit var camera: OrthographicCamera
    private lateinit var viewport: Viewport
    private lateinit var batch: SpriteBatch
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var font: BitmapFont
    private lateinit var titleFont: BitmapFont

    private val layout = GlyphLayout()

    // Progress bar properties
    private var displayProgress = 0f
    private val progressBarWidth: Float
        get() = Gdx.graphics.width * 0.6f
    private val progressBarHeight: Float
        get() = Gdx.graphics.height * 0.03f

    // Loading state
    private var assetsQueued = false
    private var minimumLoadingTime = 1.5f // Minimum time to show loading screen
    private var elapsedTime = 0f

    // Loading tips to display
    private val loadingTips = listOf(
        "Use the left joystick to move",
        "Use the right joystick to aim",
        "Defeat enemies to earn points",
        "Collect power-ups to upgrade",
        "Survive as long as you can!",
        "Watch out for asteroid storms!",
        "Drones will help you in combat",
        "Visit the shop to upgrade your ship"
    )
    private var currentTip = loadingTips.random()
    private var tipAlpha = 1f
    private var tipTimer = 0f
    private val tipDuration = 3f

    // Simple logo placeholder (will be replaced by actual logo if available)
    private var logoTexture: Texture? = null

    override fun show() {
        camera = OrthographicCamera()
        viewport = ScreenViewport(camera)
        viewport.apply()

        batch = SpriteBatch()
        shapeRenderer = ShapeRenderer()

        // Generate fonts
        val fontGenerator = FreeTypeFontGenerator(Gdx.files.internal("wheaton_capitals.otf"))

        val titleParams = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (Gdx.graphics.height * 0.08f).toInt().coerceAtLeast(32)
            color = Color.WHITE
            shadowColor = Color(0f, 0f, 0f, 0.5f)
            shadowOffsetX = 3
            shadowOffsetY = -3
        }
        titleFont = fontGenerator.generateFont(titleParams)

        val params = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (Gdx.graphics.height * 0.035f).toInt().coerceAtLeast(16)
            color = Color.WHITE
        }
        font = fontGenerator.generateFont(params)
        fontGenerator.dispose()

        // Try to load logo directly (it's small, won't cause ANR)
        try {
            if (Gdx.files.internal("logo.png").exists()) {
                logoTexture = Texture(Gdx.files.internal("logo.png"))
            }
        } catch (e: Exception) {
            Gdx.app.log("LoadingScreen", "Could not load logo: ${e.message}")
        }

        // Queue all assets for async loading
        GameAssetManager.queueAssets()
        assetsQueued = true
    }

    override fun render(delta: Float) {
        elapsedTime += delta

        // Update loading progress
        if (assetsQueued) {
            GameAssetManager.manager.update()
        }

        val actualProgress = GameAssetManager.manager.progress
        // Smooth progress animation
        displayProgress = Interpolation.smooth.apply(displayProgress, actualProgress, delta * 5f)

        // Update tip rotation
        tipTimer += delta
        if (tipTimer >= tipDuration) {
            tipTimer = 0f
            currentTip = loadingTips.random()
        }
        // Fade effect for tips
        tipAlpha = if (tipTimer < 0.3f) {
            tipTimer / 0.3f
        } else if (tipTimer > tipDuration - 0.3f) {
            (tipDuration - tipTimer) / 0.3f
        } else {
            1f
        }

        // Clear screen with dark background
        Gdx.gl.glClearColor(0.05f, 0.05f, 0.12f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        val screenWidth = Gdx.graphics.width.toFloat()
        val screenHeight = Gdx.graphics.height.toFloat()
        val centerX = screenWidth / 2f
        val centerY = screenHeight / 2f

        // Draw progress bar background
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Background bar (dark)
        shapeRenderer.color = Color(0.2f, 0.2f, 0.3f, 1f)
        shapeRenderer.rect(
            centerX - progressBarWidth / 2f,
            centerY - progressBarHeight / 2f - screenHeight * 0.1f,
            progressBarWidth,
            progressBarHeight
        )

        // Progress bar (gradient effect with two colors)
        val progressWidth = progressBarWidth * displayProgress
        shapeRenderer.color = Color(0.3f, 0.7f, 1f, 1f)
        shapeRenderer.rect(
            centerX - progressBarWidth / 2f,
            centerY - progressBarHeight / 2f - screenHeight * 0.1f,
            progressWidth,
            progressBarHeight
        )

        // Lighter accent on top of progress
        shapeRenderer.color = Color(0.5f, 0.85f, 1f, 0.6f)
        shapeRenderer.rect(
            centerX - progressBarWidth / 2f,
            centerY - screenHeight * 0.1f,
            progressWidth,
            progressBarHeight * 0.4f
        )

        shapeRenderer.end()

        // Draw progress bar border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color(0.4f, 0.5f, 0.7f, 1f)
        shapeRenderer.rect(
            centerX - progressBarWidth / 2f,
            centerY - progressBarHeight / 2f - screenHeight * 0.1f,
            progressBarWidth,
            progressBarHeight
        )
        shapeRenderer.end()

        // Draw text
        batch.begin()

        // Draw logo if available
        logoTexture?.let { logo ->
            val logoScale = (screenHeight * 0.25f) / logo.height
            val logoW = logo.width * logoScale
            val logoH = logo.height * logoScale
            batch.draw(logo, centerX - logoW / 2f, centerY + screenHeight * 0.05f, logoW, logoH)
        }

        // Title
        layout.setText(titleFont, "SIMPLE SURVIVOR")
        titleFont.draw(
            batch,
            "SIMPLE SURVIVOR",
            centerX - layout.width / 2f,
            centerY + screenHeight * 0.25f + layout.height / 2f
        )

        // Loading percentage
        val percentText = "${(displayProgress * 100).toInt()}%"
        layout.setText(font, percentText)
        font.draw(
            batch,
            percentText,
            centerX - layout.width / 2f,
            centerY - screenHeight * 0.1f - progressBarHeight - screenHeight * 0.02f
        )

        // Loading tip with fade
        font.color = Color(1f, 1f, 1f, tipAlpha * 0.8f)
        layout.setText(font, currentTip)
        font.draw(
            batch,
            currentTip,
            centerX - layout.width / 2f,
            centerY - screenHeight * 0.22f
        )
        font.color = Color.WHITE

        // "Loading..." text with dots animation
        val dots = ".".repeat(((elapsedTime * 2) % 4).toInt())
        val loadingText = "Loading$dots"
        layout.setText(font, loadingText)
        font.draw(
            batch,
            loadingText,
            centerX - layout.width / 2f,
            centerY - screenHeight * 0.05f
        )

        batch.end()

        // Check if loading is complete
        if (GameAssetManager.manager.isFinished && elapsedTime >= minimumLoadingTime) {
            goToMainMenu()
        }
    }

    private fun goToMainMenu() {
        game.screen = MainMenuScreen(game)
        dispose()
    }

    override fun resize(width: Int, height: Int) {
        viewport.update(width, height, true)
    }

    override fun pause() {}

    override fun resume() {}

    override fun hide() {}

    override fun dispose() {
        batch.dispose()
        shapeRenderer.dispose()
        font.dispose()
        titleFont.dispose()
        logoTexture?.dispose()
    }
}


