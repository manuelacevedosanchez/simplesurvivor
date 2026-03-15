package es.masmultimedia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.GlyphLayout
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.TimeUtils
import com.badlogic.gdx.utils.viewport.ScreenViewport
import es.masmultimedia.entities.Enemy
import es.masmultimedia.entities.EnemyFactory
import es.masmultimedia.entities.EnemyType
import es.masmultimedia.entities.LaserProjectile
import es.masmultimedia.entities.PowerUp
import es.masmultimedia.entities.Projectile
import es.masmultimedia.entities.ProjectileFactory
import es.masmultimedia.entities.Satellite
import es.masmultimedia.entities.Spaceship
import es.masmultimedia.entities.Star
import es.masmultimedia.game.SimpleSurvivorGame
import es.masmultimedia.utils.Constants
import es.masmultimedia.utils.GameAssetManager
import es.masmultimedia.utils.intersectsSegment
import java.util.Locale
import ktx.math.random
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class GameScreen(private val game: SimpleSurvivorGame) : Screen, InputProcessor {
    private lateinit var camera: OrthographicCamera
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var spriteBatch: SpriteBatch

    private lateinit var player: Spaceship

    private var gameStartTime = 0L
    private var gameEnded = false
    private var gameWon = false
    private var gameEndMessage = "¡Juego Terminado!"
    private var enemiesDefeated = 0
    private var score = 0
    private var isPaused = false

    private val enemies = mutableListOf<Enemy>()
    private val projectiles = mutableListOf<Projectile>()
    private val skin = Skin(Gdx.files.internal("uiskin.json"))

    private val powerUps = mutableListOf<PowerUp>()
    private var lastPowerUpSpawnTime = 0L
    private var powerUpSpawnInterval = 10000L // every 10 seconds

    private var lastShotTime = 0L
    private var lastEnemySpawnTime = 0L
    private var enemySpawnInterval = 5000L // Initial interval: 5 seconds
    private var lastPlayerDirection = Vector2(1f, 0f) // Default direction: to the right

    private lateinit var stage: Stage
    private lateinit var movementTouchpad: Touchpad
    private lateinit var rotationTouchpad: Touchpad

    // Starfield layers for parallax
    private val starsFar = mutableListOf<Star>()
    private val starsMid = mutableListOf<Star>()
    private val starsNear = mutableListOf<Star>()

    private var satellite: Satellite? = null

    private val sectorWidth = 10000f
    private val sectorHeight = 10000f

    private val playerBounds = Rectangle()

    // HUD
    private lateinit var hudStage: Stage
    private lateinit var hudFont: BitmapFont
    private lateinit var labelScore: Label
    private lateinit var labelTime: Label
    private lateinit var labelKills: Label
    private lateinit var markerCountFont: BitmapFont

    // HUD caches and reusable vectors to avoid per-frame allocations.
    private var hudScoreText = ""
    private var hudKillsText = ""
    private val projectedEnemyPos = Vector3()
    private val edgeMarkers = mutableListOf<EdgeMarker>()
    private val markerMergeDistanceSquared = 30f * 30f
    private val markerCountLayout = GlyphLayout()

    private val markerColorNormal = Color(1f, 0.35f, 0.25f, 0.9f)
    private val markerColorFast = Color(1f, 0.85f, 0.25f, 0.95f)
    private val markerColorStrong = Color(0.95f, 0.25f, 0.95f, 1f)

    override fun show() {
        camera = OrthographicCamera().apply {
            setToOrtho(false, 800f, 600f)
            position.set(0f, 0f, 0f)
        }

        shapeRenderer = ShapeRenderer()
        spriteBatch = SpriteBatch()

        player = Spaceship(
            position = Vector2(0f, 0f),
            texture = GameAssetManager.getTexture("spaceship_base.png")
        )

        gameStartTime = TimeUtils.millis()
        lastEnemySpawnTime = TimeUtils.millis()

        stage = Stage()
        Gdx.input.inputProcessor = stage

        val screenWidth = Gdx.graphics.width.toFloat()
        val screenHeight = Gdx.graphics.height.toFloat()

        // Define a margin percentage
        val marginPercentage = 0.10f // 10% of the screen size
        val marginX = screenWidth * marginPercentage
        val marginY = screenHeight * marginPercentage

        val touchpadStyle = Touchpad.TouchpadStyle().apply {
            background = skin.getDrawable("default-round")
            knob = skin.getDrawable("default-round")
        }

        val touchpadSize = screenWidth * 0.10f // 10% of the width, for example

        // Example: movement touchpad in the bottom-left corner
        // Place it with a left margin and a bottom margin
        movementTouchpad = Touchpad(10f, touchpadStyle).apply {
            setBounds(marginX, marginY, touchpadSize, touchpadSize)
        }

        // Example: rotation touchpad in the bottom-right corner
        // Subtract 200f (touchpad width) plus the margin
        rotationTouchpad = Touchpad(10f, touchpadStyle).apply {
            setBounds(
                screenWidth - 200f - marginX,
                marginY,
                touchpadSize,
                touchpadSize
            )
        }

        stage.addActor(movementTouchpad)
        stage.addActor(rotationTouchpad)

        val inputMultiplexer = InputMultiplexer(this, stage)
        Gdx.input.inputProcessor = inputMultiplexer

        setupHud()
        generateStars()
    }

    private fun setupHud() {
        hudStage = Stage(ScreenViewport())

        val generator = FreeTypeFontGenerator(Gdx.files.internal("wheaton_capitals.otf"))
        val params = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (Gdx.graphics.height * 0.045f).toInt().coerceAtLeast(14)
            color = Color.WHITE
            shadowColor = Color(0f, 0f, 0f, 0.6f)
            shadowOffsetX = 2
            shadowOffsetY = -2
        }
        hudFont = generator.generateFont(params)

        val markerParams = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (Gdx.graphics.height * 0.028f).toInt().coerceAtLeast(12)
            color = Color.WHITE
            shadowColor = Color(0f, 0f, 0f, 0.75f)
            shadowOffsetX = 1
            shadowOffsetY = -1
        }
        markerCountFont = generator.generateFont(markerParams)
        generator.dispose()

        val labelStyle = Label.LabelStyle(hudFont, Color.WHITE)

        labelKills = Label("Kills: 0", labelStyle)
        labelTime  = Label("0:00", labelStyle)
        labelScore = Label("Score: 0", labelStyle)
        labelScore.setAlignment(Align.right)

        hudScoreText = "Score: ${formatCompactNumber(score)}"
        hudKillsText = "Kills: ${formatCompactNumber(enemiesDefeated)}"
        labelScore.setText(hudScoreText)
        labelKills.setText(hudKillsText)

        val pad = Gdx.graphics.width * 0.025f
        val sideMinWidth = Gdx.graphics.width * 0.30f

        val table = Table().apply {
            setFillParent(true)
            top()
            pad(pad)
            add(labelKills).minWidth(sideMinWidth).expandX().left()
            add(labelTime).expandX().center()
            add(labelScore).minWidth(sideMinWidth).expandX().right()
        }

        hudStage.addActor(table)
    }

    private fun generateStars() {
        generateLayer(starsFar, 200)
        generateLayer(starsMid, 300)
        generateLayer(starsNear, 500)
    }

    private fun generateLayer(
        layer: MutableList<Star>,
        count: Int
    ) {
        for (i in 1..count) {
            val x = Math.random().toFloat() * sectorWidth
            val y = Math.random().toFloat() * sectorHeight
            val size = (Math.random().toFloat() * 2f) + 1f
            val alpha = (Math.random().toFloat() * 0.5f) + 0.3f
            val starColor = Color(1f, 1f, 1f, alpha)
            val star = Star(x, y, size, starColor)
            layer.add(star)
        }
    }

    private fun drawStarfield() {
        drawLayer(starsFar, 0.1f)
        drawLayer(starsMid, 0.5f)
        drawLayer(starsNear, 1.0f)
    }

    private fun drawLayer(layer: MutableList<Star>, factor: Float) {
        val camX = camera.position.x
        val camY = camera.position.y

        // Draw a 3x3 tile grid around the camera.
        // This means: the original tile and the 8 adjacent ones:
        // dx, dy ∈ {-1, 0, 1}
        for (star in layer) {
            for (ix in -1..1) {
                for (iy in -1..1) {
                    // Compute the star position in this repeated tile
                    val tileX = star.x + ix * sectorWidth
                    val tileY = star.y + iy * sectorHeight

                    shapeRenderer.color = star.color
                    val drawX = (tileX - camX) * factor + camX
                    val drawY = (tileY - camY) * factor + camY
                    shapeRenderer.circle(drawX, drawY, star.size)
                }
            }
        }
    }

    private fun spawnPowerUp() {
        if (powerUps.size >= Constants.MAX_ACTIVE_POWER_UPS) {
            return
        }

        val halfWidth = camera.viewportWidth / 2
        val halfHeight = camera.viewportHeight / 2

        val minX = player.position.x - halfWidth
        val maxX = player.position.x + halfWidth
        val minY = player.position.y - halfHeight
        val maxY = player.position.y + halfHeight

        val spawnX = (minX..maxX).random()
        val spawnY = (minY..maxY).random()

        val type = PowerUp.Type.entries.random()
        val color = when (type) {
            PowerUp.Type.HEALTH -> Color.GREEN
            PowerUp.Type.TRIPLE_SHOT -> Color.RED
            PowerUp.Type.SHIELD -> Color.CYAN
            PowerUp.Type.CHARGED_SHOT -> Color.YELLOW
            PowerUp.Type.LASER -> Color.BLUE
            PowerUp.Type.SATELLITE -> Color.WHITE
        }

        powerUps.add(PowerUp(Vector2(spawnX, spawnY), radius = 10f, color = color, type = type))
    }

    override fun render(delta: Float) {
        player.update(delta)

        if (gameEnded) {
            game.screen = GameOverScreen(
                game,
                score,
                enemiesDefeated,
                TimeUtils.timeSinceMillis(gameStartTime),
                gameEndMessage
            )
            dispose()
            return
        }

        if (isPaused) {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            stage.act(delta)
            stage.draw()
            return
        }

        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        camera.position.set(player.position.x, player.position.y, 0f)
        camera.update()

        val moveX = movementTouchpad.knobPercentX
        val moveY = movementTouchpad.knobPercentY

        if (movementTouchpad.isTouched) {
            val playerDirection = Vector2(moveX, moveY)
            if (playerDirection.len() > 0) {
                playerDirection.nor()
                player.updatePosition(playerDirection, Gdx.graphics.deltaTime)
            }
        }

        val rotX = rotationTouchpad.knobPercentX
        val rotY = rotationTouchpad.knobPercentY
        if (rotationTouchpad.isTouched && (rotX != 0f || rotY != 0f)) {
            val rotationDirection = Vector2(rotX, rotY).nor()
            player.rotation = rotationDirection.angleDeg() - 90
            lastPlayerDirection = rotationDirection
        }

        updatePlayerBounds()
        pruneProjectiles()

        val effectiveEnemySpawnInterval = getEffectiveEnemySpawnInterval()
        if (TimeUtils.timeSinceMillis(lastEnemySpawnTime) > effectiveEnemySpawnInterval) {
            val spawned = spawnEnemy()
            if (spawned) {
                lastEnemySpawnTime = TimeUtils.millis()
                if (enemySpawnInterval > 1000L) {
                    enemySpawnInterval -= 100L
                }
            }
        }

        val enemyIterator = enemies.iterator()
        while (enemyIterator.hasNext()) {
            val enemy = enemyIterator.next()
            enemy.moveTowards(player.position)

            if (enemy.bounds.overlaps(playerBounds)) {
                player.takeDamage(20)
                enemyIterator.remove()
                if (!player.isAlive()) {
                    gameEnded = true
                    gameWon = false
                    gameEndMessage = "¡Juego Terminado!"
                    return
                }
                continue
            }

            val projectileIterator = projectiles.iterator()
            while (projectileIterator.hasNext()) {
                val projectile = projectileIterator.next()
                if (projectile is LaserProjectile) {
                    continue
                }

                if (enemy.bounds.contains(projectile.position)) {
                    enemy.takeDamage(projectile.power)
                    projectileIterator.remove()
                    if (!enemy.isAlive()) {
                        killEnemy(enemy, enemyIterator)
                        break
                    }
                }
            }
        }

        if (TimeUtils.nanoTime() - lastShotTime > 500_000_000L) {
            if (rotationTouchpad.isTouched) {
                val newProjectiles = ProjectileFactory.createProjectiles(
                    player.projectileType,
                    player.position.cpy(),
                    lastPlayerDirection.cpy()
                )
                addProjectilesRespectingLimit(newProjectiles)
            }
            lastShotTime = TimeUtils.nanoTime()
        }

        val now = TimeUtils.millis()
        val projectileIterator = projectiles.iterator()
        while (projectileIterator.hasNext()) {
            val projectile = projectileIterator.next()
            projectile.update()

            if (projectile is LaserProjectile) {
                val end = projectile.getEndPoint()
                val laserEnemyIterator = enemies.iterator()

                while (laserEnemyIterator.hasNext()) {
                    val enemy = laserEnemyIterator.next()
                    if (enemy.bounds.intersectsSegment(projectile.origin, end)) {
                        projectile.tryHit(enemy)
                        if (!enemy.isAlive()) {
                            killEnemy(enemy, laserEnemyIterator)
                        }
                    }
                }
            }

            if (projectile.shouldRemove(now)) {
                projectileIterator.remove()
            }
        }

        if (TimeUtils.timeSinceMillis(lastPowerUpSpawnTime) > powerUpSpawnInterval) {
            val chance = Math.random()
            if (chance < 0.10) {
                spawnPowerUp()
            }
            lastPowerUpSpawnTime = TimeUtils.millis()
        }

        for (powerUp in powerUps) {
            powerUp.update(delta)
        }

        satellite?.update(delta, enemies, projectiles)

        val powerUpIterator = powerUps.iterator()
        while (powerUpIterator.hasNext()) {
            val pu = powerUpIterator.next()
            if (pu.overlapsWith(player)) {
                powerUpIterator.remove()
                if (pu.type == PowerUp.Type.SATELLITE) {
                    satellite = Satellite(player)
                } else {
                    player.applyPowerUp(pu)
                }
            } else if (pu.isExpired()) {
                powerUpIterator.remove()
            }
        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
// ... starfield ...
        for (pu in powerUps) {
            pu.render(shapeRenderer)
        }
// ...
        shapeRenderer.end()

        updateStars(delta)
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        drawStarfield()
        shapeRenderer.end()

        spriteBatch.projectionMatrix = camera.combined
        spriteBatch.begin()
        player.render(spriteBatch)
        for (enemy in enemies) {
            enemy.render(spriteBatch)
        }
        satellite?.render(spriteBatch)
        spriteBatch.end()

        // Draw the shield circle if it is active
        if (player.isShieldActive()) {
            shapeRenderer.projectionMatrix = camera.combined
            shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
            shapeRenderer.color = Color.CYAN
            shapeRenderer.circle(player.position.x, player.position.y, player.width)
            shapeRenderer.end()
        }

        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        drawPlayerHealthBar()
        for (projectile in projectiles) {
            projectile.render(shapeRenderer)
        }
        shapeRenderer.end()

        stage.act(delta)
        stage.draw()

        drawEnemyEdgeIndicators()

        // Update and draw HUD on top
        val elapsedMs = TimeUtils.timeSinceMillis(gameStartTime)
        val elapsedSeconds = elapsedMs / 1000L
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        labelTime.setText("%d:%02d".format(minutes, seconds))

        val nextScoreText = "Score: ${formatCompactNumber(score)}"
        if (nextScoreText != hudScoreText) {
            hudScoreText = nextScoreText
            labelScore.setText(hudScoreText)
        }

        val nextKillsText = "Kills: ${formatCompactNumber(enemiesDefeated)}"
        if (nextKillsText != hudKillsText) {
            hudKillsText = nextKillsText
            labelKills.setText(hudKillsText)
        }

        hudStage.act(delta)
        hudStage.draw()
    }

    private fun updateStars(delta: Float) {
        updateLayer(starsFar, delta, 0.1f)
        updateLayer(starsMid, delta, 0.5f)
        updateLayer(starsNear, delta, 1.0f)
    }

    private fun updateLayer(layer: MutableList<Star>, delta: Float, factor: Float) {
        val twinkleSpeed = 3.0f

        for (star in layer) {
            // Twinkle effect
            star.color.a += star.twinkleDirection * twinkleSpeed * delta
            if (star.color.a > 1f) {
                star.color.a = 1f
                star.twinkleDirection = -1
            } else if (star.color.a < 0.05f) {
                star.color.a = 0.05f
                star.twinkleDirection = 1
            }

            // Global wrap-around using modulo
            // Helper function for wrap-around behavior
            star.x = wrap(star.x, sectorWidth)
            star.y = wrap(star.y, sectorHeight)
        }
    }

    // Helper wrap function
    private fun wrap(value: Float, max: Float): Float {
        var v = value % max
        if (v < 0) v += max
        return v
    }

    private fun getRandomEnemyType(): EnemyType {
        return EnemyType.entries.random()
    }

    private fun showPauseMenu() {
        val dialog = object : Dialog("Pausa", skin) {
            override fun result(obj: Any?) {
                if (obj == null) return
                if (obj as Boolean) {
                    isPaused = false
                    hide()
                } else {
                    game.screen = MainMenuScreen(game)
                    dispose()
                }
            }
        }
        dialog.text("Juego en pausa")
        dialog.button("Reanudar", true)
        dialog.button("Salir al menú", false)
        dialog.show(stage)
    }

    private fun drawPlayerHealthBar() {
        val healthPercentage = player.currentHealth.toFloat() / player.maxHealth.toFloat()
        val healthColor = Color(
            1 - healthPercentage,
            healthPercentage,
            0f,
            1f
        )
        shapeRenderer.color = healthColor

        val barWidth = player.width
        val barHeight = 5f
        val barX = player.position.x - barWidth / 2
        val barY = player.position.y - player.height / 2 - barHeight - 5f

        shapeRenderer.rect(barX, barY, barWidth * healthPercentage, barHeight)
    }

    private fun spawnEnemy(): Boolean {
        if (enemies.size >= Constants.MAX_ACTIVE_ENEMIES) {
            return false
        }

        val minSpawnDistance = 500f
        val maxSpawnDistance = 1000f

        val angle = Math.random() * 2 * Math.PI
        val distance =
            minSpawnDistance + Math.random().toFloat() * (maxSpawnDistance - minSpawnDistance)

        val spawnX = player.position.x + distance * cos(angle).toFloat()
        val spawnY = player.position.y + distance * sin(angle).toFloat()

        val enemyType = getRandomEnemyType()
        val newEnemy = EnemyFactory.createEnemy(enemyType, Vector2(spawnX, spawnY))
        enemies.add(newEnemy)
        return true
    }

    private fun getEffectiveEnemySpawnInterval(): Long {
        val occupancy = enemies.size.toFloat() / Constants.MAX_ACTIVE_ENEMIES.toFloat()
        val multiplier = when {
            occupancy >= 0.95f -> 4.0f
            occupancy >= 0.85f -> 2.6f
            occupancy >= 0.70f -> 1.6f
            else -> 1.0f
        }
        return (enemySpawnInterval * multiplier).toLong().coerceAtLeast(enemySpawnInterval)
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
        hudStage.viewport.update(width, height, true)
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun dispose() {
        player.dispose()
        spriteBatch.dispose()
        shapeRenderer.dispose()
        stage.dispose()
        hudStage.dispose()
        hudFont.dispose()
        markerCountFont.dispose()
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
            if (!isPaused) {
                isPaused = true
                showPauseMenu()
            }
            return true
        }
        return false
    }

    private fun killEnemy(enemy: Enemy, enemyIterator: MutableIterator<Enemy>) {
        enemyIterator.remove()

        val enemyScore = when (enemy.type) {
            EnemyType.NORMAL -> Constants.SCORE_ENEMY_NORMAL
            EnemyType.FAST -> Constants.SCORE_ENEMY_FAST
            EnemyType.STRONG -> Constants.SCORE_ENEMY_STRONG
        }

        val scoreWillOverflow = score > Int.MAX_VALUE - enemyScore
        val killsWillOverflow = enemiesDefeated == Int.MAX_VALUE
        if (scoreWillOverflow || killsWillOverflow) {
            score = Int.MAX_VALUE
            enemiesDefeated = Int.MAX_VALUE
            gameEnded = true
            gameEndMessage = "HAS MATADO EL NUMERO MAXIMO DE ENEMIGOS"
            return
        }

        enemiesDefeated++
        score += enemyScore

        val dropChance = when (enemy.type) {
            EnemyType.NORMAL -> 0.9
            EnemyType.FAST -> 0.9
            EnemyType.STRONG -> 0.9
        }

        if (Math.random() < dropChance && powerUps.size < Constants.MAX_ACTIVE_POWER_UPS) {
            val type = when (enemy.type) {
                EnemyType.NORMAL -> PowerUp.Type.SATELLITE
                EnemyType.FAST -> PowerUp.Type.TRIPLE_SHOT
                EnemyType.STRONG -> PowerUp.Type.LASER
            }

            val color = when (type) {
                PowerUp.Type.HEALTH -> Color.GREEN
                PowerUp.Type.TRIPLE_SHOT -> Color.RED
                PowerUp.Type.SHIELD -> Color.CYAN
                PowerUp.Type.CHARGED_SHOT -> Color.YELLOW
                PowerUp.Type.LASER -> Color.BLUE
                PowerUp.Type.SATELLITE -> Color.WHITE
            }

            powerUps.add(
                PowerUp(
                    enemy.position.cpy(),
                    radius = 10f,
                    color = color,
                    type = type
                )
            )
        }
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

    private fun updatePlayerBounds() {
        playerBounds.set(
            player.position.x - player.width / 2,
            player.position.y - player.height / 2,
            player.width,
            player.height
        )
    }

    private fun pruneProjectiles() {
        val now = TimeUtils.millis()
        val projectileIterator = projectiles.iterator()
        while (projectileIterator.hasNext()) {
            if (projectileIterator.next().shouldRemove(now)) {
                projectileIterator.remove()
            }
        }

        val overflow = projectiles.size - Constants.MAX_ACTIVE_PROJECTILES
        if (overflow > 0) {
            projectiles.subList(0, overflow).clear()
        }
    }

    private fun addProjectilesRespectingLimit(newProjectiles: List<Projectile>) {
        val availableSlots = Constants.MAX_ACTIVE_PROJECTILES - projectiles.size
        if (availableSlots <= 0) {
            return
        }

        val count = minOf(availableSlots, newProjectiles.size)
        for (index in 0 until count) {
            projectiles.add(newProjectiles[index])
        }
    }

    private fun formatCompactNumber(value: Int): String {
        val absValue = abs(value.toLong())
        val sign = if (value < 0) "-" else ""

        if (absValue >= 1_000_000_000L) {
            val formatted = String.format(Locale.US, "%.1f", absValue / 1_000_000_000.0).removeSuffix(".0")
            return "$sign${formatted}B"
        }
        if (absValue >= 1_000_000L) {
            val formatted = String.format(Locale.US, "%.1f", absValue / 1_000_000.0).removeSuffix(".0")
            return "$sign${formatted}M"
        }
        if (absValue >= 1_000L) {
            val formatted = String.format(Locale.US, "%.1f", absValue / 1_000.0).removeSuffix(".0")
            return "$sign${formatted}K"
        }
        return "$sign$absValue"
    }

    private fun drawEnemyEdgeIndicators() {
        if (enemies.isEmpty()) return

        val camX = camera.position.x
        val camY = camera.position.y
        val halfWidth = camera.viewportWidth * 0.5f
        val halfHeight = camera.viewportHeight * 0.5f

        val hudWidth = hudStage.viewport.worldWidth
        val hudHeight = hudStage.viewport.worldHeight
        val margin = 24f
        val topReserved = hudHeight * 0.12f
        val centerX = hudWidth * 0.5f
        val centerY = hudHeight * 0.5f

        edgeMarkers.clear()

        for (enemy in enemies) {
            val dx = enemy.position.x - camX
            val dy = enemy.position.y - camY

            // Skip enemies already visible in camera bounds.
            if (abs(dx) <= halfWidth && abs(dy) <= halfHeight) continue

            val tx = if (dx != 0f) halfWidth / abs(dx) else Float.POSITIVE_INFINITY
            val ty = if (dy != 0f) halfHeight / abs(dy) else Float.POSITIVE_INFINITY
            val t = minOf(tx, ty)
            if (!t.isFinite()) continue

            projectedEnemyPos.set(camX + dx * t, camY + dy * t, 0f)
            camera.project(projectedEnemyPos)

            val markerX = projectedEnemyPos.x.coerceIn(margin, hudWidth - margin)
            val markerY = projectedEnemyPos.y.coerceIn(margin, hudHeight - topReserved)

            var merged = false
            for (marker in edgeMarkers) {
                val mx = marker.x - markerX
                val my = marker.y - markerY
                if (mx * mx + my * my <= markerMergeDistanceSquared) {
                    val total = marker.count + 1
                    marker.x = (marker.x * marker.count + markerX) / total
                    marker.y = (marker.y * marker.count + markerY) / total
                    marker.count = total
                    if (enemyTypePriority(enemy.type) > enemyTypePriority(marker.enemyType)) {
                        marker.enemyType = enemy.type
                    }
                    merged = true
                    break
                }
            }

            if (!merged) {
                edgeMarkers.add(EdgeMarker(markerX, markerY, enemy.type, 1))
            }
        }

        shapeRenderer.projectionMatrix = hudStage.camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        for (marker in edgeMarkers) {
            shapeRenderer.color = when (marker.enemyType) {
                EnemyType.NORMAL -> markerColorNormal
                EnemyType.FAST -> markerColorFast
                EnemyType.STRONG -> markerColorStrong
            }
            val size = 10f + minOf(8f, (marker.count - 1) * 1.5f)
            drawIndicatorTriangle(marker.x, marker.y, centerX, centerY, size)
        }

        shapeRenderer.end()

        // Draw group counts for merged markers.
        hudStage.batch.projectionMatrix = hudStage.camera.combined
        hudStage.batch.begin()
        for (marker in edgeMarkers) {
            if (marker.count <= 1) continue

            val countText = "x${marker.count}"
            markerCountLayout.setText(markerCountFont, countText)
            val textX = marker.x - markerCountLayout.width * 0.5f
            val textY = marker.y - 14f
            markerCountFont.draw(hudStage.batch, countText, textX, textY)
        }
        hudStage.batch.end()
    }

    private fun enemyTypePriority(type: EnemyType): Int {
        return when (type) {
            EnemyType.NORMAL -> 1
            EnemyType.FAST -> 2
            EnemyType.STRONG -> 3
        }
    }

    private fun drawIndicatorTriangle(markerX: Float, markerY: Float, centerX: Float, centerY: Float, size: Float) {
        val dx = markerX - centerX
        val dy = markerY - centerY
        val lengthSquared = dx * dx + dy * dy
        if (lengthSquared < 0.0001f) return

        val invLength = 1f / sqrt(lengthSquared)
        val dirX = dx * invLength
        val dirY = dy * invLength
        val perpX = -dirY
        val perpY = dirX

        val baseDistance = size * 1.8f
        val halfWidth = size * 0.9f

        val tipX = markerX
        val tipY = markerY
        val baseX = tipX - dirX * baseDistance
        val baseY = tipY - dirY * baseDistance

        val leftX = baseX + perpX * halfWidth
        val leftY = baseY + perpY * halfWidth
        val rightX = baseX - perpX * halfWidth
        val rightY = baseY - perpY * halfWidth

        shapeRenderer.triangle(tipX, tipY, leftX, leftY, rightX, rightY)
    }

    private data class EdgeMarker(
        var x: Float,
        var y: Float,
        var enemyType: EnemyType,
        var count: Int
    )
}
