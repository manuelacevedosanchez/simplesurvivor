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
import es.masmultimedia.entities.Drone
import es.masmultimedia.entities.EnemyType
import es.masmultimedia.entities.LaserProjectile
import es.masmultimedia.entities.PowerUp
import es.masmultimedia.entities.ProceduralEnemy
import es.masmultimedia.entities.ProceduralEnemyFactory
import es.masmultimedia.entities.Projectile
import es.masmultimedia.entities.ProjectileFactory
import es.masmultimedia.entities.Satellite
import es.masmultimedia.entities.Spaceship
import es.masmultimedia.entities.Star
import es.masmultimedia.game.SimpleSurvivorGame
import es.masmultimedia.utils.Constants
import es.masmultimedia.utils.GameAssetManager
import es.masmultimedia.utils.JoystickRenderer
import es.masmultimedia.utils.intersectsSegment
import ktx.math.random
import java.util.Locale
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

    private val enemies = mutableListOf<ProceduralEnemy>()
    private val projectiles = mutableListOf<Projectile>()
    private val skin = Skin(Gdx.files.internal("uiskin.json"))

    private val powerUps = mutableListOf<PowerUp>()
    private var lastPowerUpSpawnTime = 0L
    private var powerUpSpawnInterval = 10000L // every 10 seconds

    private var lastShotTime = 0L
    private var lastEnemySpawnTime = 0L
    private var enemySpawnInterval = 5000L // Initial interval: 5 seconds
    private var lastPlayerDirection = Vector2(1f, 0f) // Default direction: to the right

    // Runtime upgrades from shop purchases.
    private var moveSpeedMultiplier = 1f
    private var damageTakenMultiplier = 1f
    private var shotCooldownNanos = 500_000_000L

    // Shop progression by score milestones.
    private var nextShopMilestoneScore = Constants.SHOP_FIRST_MILESTONE_SCORE

    // Peak score ever reached this run – used to avoid re-triggering the shop
    // if the player spends score and climbs back to the same milestone.
    private var peakScore = 0
    private var shopOpen = false
    private var pendingShopOpen = false

    // Timed asteroid storm event.
    private val asteroids = mutableListOf<Asteroid>()
    private var nextStormStartTime = 0L
    private var stormActive = false
    private var stormEndTime = 0L
    private var lastAsteroidSpawnTime = 0L

    private lateinit var stage: Stage
    private lateinit var movementTouchpad: Touchpad
    private lateinit var rotationTouchpad: Touchpad

    private val joystickRenderer = JoystickRenderer()

    // Starfield layers for parallax
    private val starsFar = mutableListOf<Star>()
    private val starsMid = mutableListOf<Star>()
    private val starsNear = mutableListOf<Star>()

    private var satellite: Satellite? = null
    private val drones = mutableListOf<Drone>()
    private val maxDrones = 4 // Maximum allowed drones

    private val sectorWidth = 10000f
    private val sectorHeight = 10000f

    private val playerBounds = Rectangle()

    // HUD
    private lateinit var hudStage: Stage
    private lateinit var hudFont: BitmapFont
    private lateinit var hudStatsFont: BitmapFont
    private lateinit var labelScore: Label
    private lateinit var labelTime: Label
    private lateinit var labelKills: Label
    private lateinit var labelEvent: Label
    private lateinit var labelStatHp: Label
    private lateinit var labelStatSpeed: Label
    private lateinit var labelStatFireRate: Label
    private lateinit var labelStatDmgReduc: Label
    private lateinit var labelStatWeapon: Label
    private lateinit var labelActivePowerUps: Label
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

    // Storm telegraph window shown before each asteroid storm starts.
    private val stormTelegraphMs = 3_000L

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
        nextStormStartTime = gameStartTime + Constants.ASTEROID_STORM_INTERVAL_MS

        stage = Stage()
        Gdx.input.inputProcessor = stage

        val screenWidth = Gdx.graphics.width.toFloat()
        val screenHeight = Gdx.graphics.height.toFloat()

        // Define a margin percentage
        val marginPercentage = 0.10f // 10% of the screen size
        val marginX = screenWidth * marginPercentage
        val marginY = screenHeight * marginPercentage

        // Create invisible touchpad style (no visible drawables)
        val touchpadStyle = Touchpad.TouchpadStyle().apply {
            // Leave background and knob null for invisible touchpads
            background = null
            knob = null
        }

        val touchpadSize = screenWidth * 0.18f // Slightly larger for better usability

        // Example: movement touchpad in the bottom-left corner
        // Place it with a left margin and a bottom margin
        movementTouchpad = Touchpad(10f, touchpadStyle).apply {
            setBounds(marginX, marginY, touchpadSize, touchpadSize)
        }

        // Example: rotation touchpad in the bottom-right corner
        // Subtract 200f (touchpad width) plus the margin
        rotationTouchpad = Touchpad(10f, touchpadStyle).apply {
            setBounds(
                screenWidth - touchpadSize - marginX,
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

        val statsParams = FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (Gdx.graphics.height * 0.028f).toInt().coerceAtLeast(11)
            color = Color.WHITE
            shadowColor = Color(0f, 0f, 0f, 0.7f)
            shadowOffsetX = 1
            shadowOffsetY = -1
        }
        hudStatsFont = generator.generateFont(statsParams)

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
        val eventLabelStyle = Label.LabelStyle(hudFont, Color(1f, 0.55f, 0.25f, 1f))
        val statsLabelStyle = Label.LabelStyle(hudStatsFont, Color(0.85f, 0.85f, 0.85f, 0.9f))
        val statsValueStyle = Label.LabelStyle(hudStatsFont, Color(0.6f, 1f, 0.6f, 1f))

        labelKills = Label("Kills: 0", labelStyle)
        labelTime = Label("0:00", labelStyle)
        labelScore = Label("Score: 0", labelStyle)
        labelEvent = Label("", eventLabelStyle)
        labelScore.setAlignment(Align.right)
        labelEvent.setAlignment(Align.center)

        hudScoreText = "Score: ${formatCompactNumber(score)}"
        hudKillsText = "Kills: ${formatCompactNumber(enemiesDefeated)}"
        labelScore.setText(hudScoreText)
        labelKills.setText(hudKillsText)

        // Live ship/weapon stats labels (values updated every frame).
        labelStatHp = Label("100 / 100", statsValueStyle)
        labelStatSpeed = Label("100%", statsValueStyle)
        labelStatFireRate = Label("500 ms", statsValueStyle)
        labelStatDmgReduc = Label("-0%", statsValueStyle)
        labelStatWeapon = Label("BASIC", statsValueStyle)
        labelActivePowerUps = Label("-", statsValueStyle)

        // Stats sub-table – two columns: label name | value
        val statsTable = Table()
        fun statRow(name: String, valueLabel: Label) {
            statsTable.add(Label(name, statsLabelStyle)).left().padRight(6f)
            statsTable.add(valueLabel).left().row()
        }
        statRow("HP:", labelStatHp)
        statRow("Speed:", labelStatSpeed)
        statRow("Fire:", labelStatFireRate)
        statRow("Armor:", labelStatDmgReduc)
        statRow("Weapon:", labelStatWeapon)
        statRow("Active:", labelActivePowerUps)

        val pad = Gdx.graphics.width * 0.025f
        val sideMinWidth = Gdx.graphics.width * 0.30f

        // Root table: top bar (kills | time | score) + left stats panel below
        val root = Table().apply {
            setFillParent(true)
            top()
            pad(pad)
        }

        // Top row
        root.add(labelKills).minWidth(sideMinWidth).expandX().left()
        root.add(labelTime).expandX().center()
        root.add(labelScore).minWidth(sideMinWidth).expandX().right()
        root.row()

        // Stats panel anchored top-left, second row
        root.add(statsTable).top().left().padTop(pad * 0.5f)
        root.add()  // empty center cell
        root.add()  // empty right cell
        root.row()

        // Event telegraph row centered under top HUD.
        root.add()
        root.add(labelEvent).padTop(pad * 0.35f).center()
        root.add()

        hudStage.addActor(root)
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

        val type = selectRandomPowerUpByRarity()
        val color = PowerUp.getColor(type)

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
        val nowMillis = TimeUtils.millis()

        val moveX = movementTouchpad.knobPercentX
        val moveY = movementTouchpad.knobPercentY

        if (movementTouchpad.isTouched) {
            val playerDirection = Vector2(moveX, moveY)
            if (playerDirection.len() > 0) {
                playerDirection.nor()
                player.updatePosition(playerDirection, Gdx.graphics.deltaTime * moveSpeedMultiplier)
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
        updateAsteroidStorm(nowMillis, delta)

        if (pendingShopOpen && !shopOpen) {
            pendingShopOpen = false
            showShopDialog()
            return
        }

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
        val timeSlowFactor = player.getTimeSlowFactor()
        while (enemyIterator.hasNext()) {
            val enemy = enemyIterator.next()
            enemy.update(delta)
            enemy.moveTowards(player.position, timeSlowFactor)

            if (enemy.bounds.overlaps(playerBounds)) {
                val contactDamage = (20f * damageTakenMultiplier).toInt().coerceAtLeast(1)
                player.takeDamage(contactDamage)
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
                    // If piercing is active, the projectile is not removed
                    if (!player.isPiercingActive()) {
                        projectileIterator.remove()
                    }
                    if (!enemy.isAlive()) {
                        killEnemy(enemy, enemyIterator)
                        break
                    }
                }
            }
        }

        val effectiveCooldown = (shotCooldownNanos * player.getShotCooldownMultiplier()).toLong()
        if (TimeUtils.nanoTime() - lastShotTime > effectiveCooldown) {
            if (rotationTouchpad.isTouched) {
                var newProjectiles = ProjectileFactory.createProjectiles(
                    player.projectileType,
                    player.position.cpy(),
                    lastPlayerDirection.cpy()
                )

                // Mirror shot: add projectiles in opposite direction
                if (player.isMirrorShotActive()) {
                    val mirrorDirection = lastPlayerDirection.cpy().scl(-1f)
                    val mirrorProjectiles = ProjectileFactory.createProjectiles(
                        player.projectileType,
                        player.position.cpy(),
                        mirrorDirection
                    )
                    newProjectiles = newProjectiles + mirrorProjectiles
                }

                addProjectilesRespectingLimit(newProjectiles)
            }
            lastShotTime = TimeUtils.nanoTime()
        }

        val now = TimeUtils.millis()
        val projectileIterator = projectiles.iterator()
        while (projectileIterator.hasNext()) {
            val projectile = projectileIterator.next()

            // Efecto homing: los proyectiles persiguen al enemigo más cercano
            if (player.isHomingActive() && projectile !is LaserProjectile) {
                val nearestEnemy = enemies.minByOrNull { it.position.dst2(projectile.position) }
                if (nearestEnemy != null) {
                    projectile.homeTowards(nearestEnemy.position)
                }
            }

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
        for (drone in drones) {
            drone.update(delta, enemies, projectiles)
        }

        val powerUpIterator = powerUps.iterator()
        while (powerUpIterator.hasNext()) {
            val pu = powerUpIterator.next()
            if (pu.overlapsWith(player)) {
                powerUpIterator.remove()
                when (pu.type) {
                    PowerUp.Type.SATELLITE -> satellite = Satellite(player)
                    PowerUp.Type.DRONE -> spawnDrone()
                    PowerUp.Type.BOMB -> applyBombEffect()
                    else -> player.applyPowerUp(pu)
                }
            } else if (pu.isExpired()) {
                powerUpIterator.remove()
            }
        }

        // Magnet effect: attract nearby power-ups
        if (player.isMagnetActive()) {
            for (pu in powerUps) {
                val dist = Vector2.dst(player.position.x, player.position.y, pu.position.x, pu.position.y)
                if (dist < player.magnetRange && dist > 0) {
                    val direction = Vector2(player.position.x - pu.position.x, player.position.y - pu.position.y)
                    direction.nor().scl(200f * delta)
                    pu.position.add(direction)
                }
            }
        }

        updateStars(delta)
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        drawStarfield()
        drawAsteroids()
        // Render power-ups in world space (camera.combined already set above).
        for (pu in powerUps) {
            pu.render(shapeRenderer)
        }
        // Render procedural enemies
        for (enemy in enemies) {
            enemy.render(shapeRenderer)
        }
        shapeRenderer.end()

        spriteBatch.projectionMatrix = camera.combined
        spriteBatch.begin()
        player.render(spriteBatch)
        satellite?.render(spriteBatch)
        for (drone in drones) {
            drone.render(spriteBatch)
        }
        spriteBatch.end()

        // Draw visual effects for active power-ups
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)

        // Shield - círculo cyan
        if (player.isShieldActive()) {
            shapeRenderer.color = Color.CYAN
            shapeRenderer.circle(player.position.x, player.position.y, player.width)
        }

        // Invincibility - círculo dorado más grande
        if (player.isInvincibilityActive()) {
            shapeRenderer.color = Color.GOLD
            shapeRenderer.circle(player.position.x, player.position.y, player.width * 1.2f)
        }

        // Magnet - círculo magenta mostrando el rango
        if (player.isMagnetActive()) {
            shapeRenderer.color = Color.MAGENTA
            Gdx.gl.glLineWidth(1f)
            shapeRenderer.circle(player.position.x, player.position.y, player.magnetRange)
        }

        // Time slow - efecto visual púrpura
        if (player.isTimeSlowActive()) {
            shapeRenderer.color = Color.PURPLE
            shapeRenderer.circle(player.position.x, player.position.y, player.width * 0.8f)
        }

        shapeRenderer.end()

        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        drawPlayerHealthBar()
        for (projectile in projectiles) {
            projectile.render(shapeRenderer)
        }
        shapeRenderer.end()

        stage.act(delta)
        stage.draw()

        // Draw custom joysticks on top of stage
        drawJoysticks()

        drawEnemyEdgeIndicators()

        // Update and draw HUD on top
        val elapsedMs = TimeUtils.timeSinceMillis(gameStartTime)
        val elapsedSeconds = elapsedMs / 1000L
        val minutes = elapsedSeconds / 60
        val seconds = elapsedSeconds % 60
        labelTime.setText("%d:%02d".format(minutes, seconds))

        // Telegraph / active storm status.
        val hudNow = TimeUtils.millis()
        val telegraphRemaining = nextStormStartTime - hudNow
        if (stormActive) {
            labelEvent.setText("ASTEROID STORM!")
        } else if (telegraphRemaining in 1..stormTelegraphMs) {
            val secLeft = ((telegraphRemaining + 999L) / 1000L).coerceAtLeast(1L)
            labelEvent.setText("STORM INCOMING: ${secLeft}s")
        } else {
            labelEvent.setText("")
        }

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

        // Update live ship/weapon stats (only when values change to avoid string alloc every frame).
        val hpText = "${player.currentHealth} / ${player.maxHealth}"
        if (labelStatHp.text.toString() != hpText) labelStatHp.setText(hpText)

        val speedText = "${(moveSpeedMultiplier * 100).toInt()}%"
        if (labelStatSpeed.text.toString() != speedText) labelStatSpeed.setText(speedText)

        val fireText = "${shotCooldownNanos / 1_000_000L} ms"
        if (labelStatFireRate.text.toString() != fireText) labelStatFireRate.setText(fireText)

        val armorText = "-${((1f - damageTakenMultiplier) * 100).toInt().coerceAtLeast(0)}%"
        if (labelStatDmgReduc.text.toString() != armorText) labelStatDmgReduc.setText(armorText)

        val weaponText = player.projectileType.name
        if (labelStatWeapon.text.toString() != weaponText) labelStatWeapon.setText(weaponText)

        // Update active power-ups display
        val activePowerUpsText = buildActivePowerUpsText()
        if (labelActivePowerUps.text.toString() != activePowerUpsText) {
            labelActivePowerUps.setText(activePowerUpsText)
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
        val newEnemy = ProceduralEnemyFactory.createEnemy(enemyType, Vector2(spawnX, spawnY))
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
        hudStatsFont.dispose()
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

    private fun killEnemy(enemy: ProceduralEnemy, enemyIterator: MutableIterator<ProceduralEnemy>) {
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
        score += enemyScore * player.getScoreMultiplier()

        // Update peak score and check shop milestone against it,
        // so spending score and recovering doesn't re-trigger the same milestone.
        if (score > peakScore) peakScore = score
        if (peakScore >= nextShopMilestoneScore) {
            pendingShopOpen = true
            nextShopMilestoneScore += Constants.SHOP_MILESTONE_STEP_SCORE
        }

        // Drop chance varies by enemy type: stronger enemies have better drop rates
        val dropChance = when (enemy.type) {
            EnemyType.NORMAL -> 0.12  // 12% - common enemy, low drop
            EnemyType.FAST -> 0.18    // 18% - harder to kill, slightly better
            EnemyType.STRONG -> 0.25  // 25% - tanky enemy, best drop rate
        }

        if (Math.random() < dropChance && powerUps.size < Constants.MAX_ACTIVE_POWER_UPS) {
            // Each enemy type has weighted probabilities for different power-up types
            val type = selectWeightedPowerUp(enemy.type)
            val color = PowerUp.getColor(type)

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

    /**
     * Selects a power-up type using weighted probabilities based on enemy type.
     * Stronger enemies have better chances of dropping rare power-ups.
     */
    private fun selectWeightedPowerUp(enemyType: EnemyType): PowerUp.Type {
        // Rarity multiplier based on enemy type
        val rarityBoost = when (enemyType) {
            EnemyType.NORMAL -> 0.8f  // More common drops
            EnemyType.FAST -> 1.0f    // Normal drops
            EnemyType.STRONG -> 1.4f  // Rarer drops (better quality)
        }

        // Category boost based on enemy type
        val categoryBoost = when (enemyType) {
            EnemyType.NORMAL -> mapOf(
                PowerUp.Category.DEFENSIVE to 1.6f,   // Favors health/defense
                PowerUp.Category.OFFENSIVE to 0.9f,
                PowerUp.Category.UTILITY to 0.7f,
                PowerUp.Category.SPECIAL to 0.5f
            )
            EnemyType.FAST -> mapOf(
                PowerUp.Category.DEFENSIVE to 1.0f,
                PowerUp.Category.OFFENSIVE to 1.4f,   // Favors offensive
                PowerUp.Category.UTILITY to 1.2f,
                PowerUp.Category.SPECIAL to 0.8f
            )
            EnemyType.STRONG -> mapOf(
                PowerUp.Category.DEFENSIVE to 0.7f,
                PowerUp.Category.OFFENSIVE to 1.3f,
                PowerUp.Category.UTILITY to 1.0f,
                PowerUp.Category.SPECIAL to 1.8f      // Favors special/rare
            )
        }

        // Calculate final weights
        val weights = PowerUp.Type.entries.map { type ->
            val baseWeight = type.rarity * 100
            val catBoost = categoryBoost[type.category] ?: 1.0f
            (baseWeight * rarityBoost * catBoost).toInt().coerceAtLeast(1)
        }

        val totalWeight = weights.sum()
        val roll = (Math.random() * totalWeight).toInt()

        var cumulative = 0
        PowerUp.Type.entries.forEachIndexed { index, type ->
            cumulative += weights[index]
            if (roll < cumulative) {
                return type
            }
        }

        return PowerUp.Type.HEALTH
    }

    /**
     * Selects a random power-up based on rarity weights (for timed spawns).
     */
    private fun selectRandomPowerUpByRarity(): PowerUp.Type {
        val weights = PowerUp.Type.entries.map { type ->
            (type.rarity * 100).toInt().coerceAtLeast(1)
        }

        val totalWeight = weights.sum()
        val roll = (Math.random() * totalWeight).toInt()

        var cumulative = 0
        PowerUp.Type.entries.forEachIndexed { index, type ->
            cumulative += weights[index]
            if (roll < cumulative) {
                return type
            }
        }

        return PowerUp.Type.HEALTH
    }

    /**
     * Builds the text showing active temporary power-ups with remaining time.
     */
    private fun buildActivePowerUpsText(): String {
        val activePowerUps = player.getActivePowerUps()

        // Also add permanent power-ups (satellite, drones)
        val permanentItems = mutableListOf<String>()
        if (satellite != null) permanentItems.add("SAT")
        if (drones.isNotEmpty()) permanentItems.add("DRN:${drones.size}")

        if (activePowerUps.isEmpty() && permanentItems.isEmpty()) {
            return "-"
        }

        val parts = mutableListOf<String>()

        // First the permanent ones
        parts.addAll(permanentItems)

        // Then the temporary ones with remaining time
        for ((icon, remainingMs) in activePowerUps) {
            val seconds = (remainingMs / 1000).coerceAtLeast(0)
            parts.add("$icon:${seconds}s")
        }

        return parts.joinToString(" ")
    }

    /**
     * Spawns a new drone that follows and assists the player.
     */
    private fun spawnDrone() {
        if (drones.size < maxDrones) {
            drones.add(Drone(player, drones.size))
        }
    }

    /**
     * Applies bomb effect: damages all enemies on screen.
     */
    private fun applyBombEffect() {
        val halfWidth = camera.viewportWidth / 2
        val halfHeight = camera.viewportHeight / 2
        val bombDamage = 50

        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            // Only affects enemies on screen
            val dx = abs(enemy.position.x - player.position.x)
            val dy = abs(enemy.position.y - player.position.y)
            if (dx < halfWidth && dy < halfHeight) {
                enemy.takeDamage(bombDamage)
                if (!enemy.isAlive()) {
                    killEnemy(enemy, iterator)
                }
            }
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
            val formatted =
                String.format(Locale.US, "%.1f", absValue / 1_000_000_000.0).removeSuffix(".0")
            return "$sign${formatted}B"
        }
        if (absValue >= 1_000_000L) {
            val formatted =
                String.format(Locale.US, "%.1f", absValue / 1_000_000.0).removeSuffix(".0")
            return "$sign${formatted}M"
        }
        if (absValue >= 1_000L) {
            val formatted = String.format(Locale.US, "%.1f", absValue / 1_000.0).removeSuffix(".0")
            return "$sign${formatted}K"
        }
        return "$sign$absValue"
    }

    private fun updateAsteroidStorm(nowMillis: Long, delta: Float) {
        if (!stormActive && nowMillis >= nextStormStartTime) {
            stormActive = true
            // Random storm duration between 5 and 10 seconds.
            val stormDurationMs = (5_000L..10_000L).random()
            stormEndTime = nowMillis + stormDurationMs
            lastAsteroidSpawnTime = 0L
        }

        if (stormActive && nowMillis >= stormEndTime) {
            stormActive = false
            nextStormStartTime = nowMillis + Constants.ASTEROID_STORM_INTERVAL_MS
            // Clear any leftover asteroids when the storm ends.
            asteroids.clear()
        }

        if (stormActive && nowMillis - lastAsteroidSpawnTime >= Constants.ASTEROID_STORM_SPAWN_INTERVAL_MS) {
            spawnAsteroid()
            lastAsteroidSpawnTime = nowMillis
        }

        val playerRadius = player.width * 0.35f
        val asteroidIterator = asteroids.iterator()
        while (asteroidIterator.hasNext()) {
            val asteroid = asteroidIterator.next()
            asteroid.position.mulAdd(asteroid.velocity, delta)
            asteroid.lifeSeconds -= delta

            // Check projectile hits on this asteroid.
            var destroyedByProjectile = false
            val projectileIterator = projectiles.iterator()
            while (projectileIterator.hasNext()) {
                val projectile = projectileIterator.next()
                if (projectile is LaserProjectile) continue
                val hitRadius = asteroid.radius + projectile.size
                if (asteroid.position.dst2(projectile.position) <= hitRadius * hitRadius) {
                    projectileIterator.remove()
                    asteroid.hp -= projectile.power
                    if (asteroid.hp <= 0) {
                        destroyedByProjectile = true
                        break
                    }
                }
            }
            if (destroyedByProjectile) {
                asteroidIterator.remove()
                continue
            }

            // Check player collision.
            val hitDistance = asteroid.radius + playerRadius
            if (asteroid.position.dst2(player.position) <= hitDistance * hitDistance) {
                val damage = (asteroid.damage * damageTakenMultiplier).toInt().coerceAtLeast(1)
                player.takeDamage(damage)
                asteroidIterator.remove()

                if (!player.isAlive()) {
                    gameEnded = true
                    gameEndMessage = "Destroyed by asteroid storm"
                    return
                }
                continue
            }

            if (asteroid.lifeSeconds <= 0f) {
                asteroidIterator.remove()
            }
        }
    }

    private fun spawnAsteroid() {
        val angle = Math.random().toFloat() * 360f
        val direction = Vector2(1f, 0f).setAngleDeg(angle)
        val spawnDistance = maxOf(camera.viewportWidth, camera.viewportHeight) * 0.8f + 140f

        val spawnPos = Vector2(player.position).mulAdd(direction, spawnDistance)
        val travelDirection = Vector2(player.position).sub(spawnPos).nor()

        val radius = (18f..36f).random()
        val speed = (160f..260f).random()
        val damage = (12..22).random()

        asteroids.add(
            Asteroid(
                position = spawnPos,
                velocity = travelDirection.scl(speed),
                radius = radius,
                damage = damage,
                lifeSeconds = 8f
            )
        )
    }

    private fun drawAsteroids() {
        if (asteroids.isEmpty()) return

        for (asteroid in asteroids) {
            shapeRenderer.color = Color(0.45f, 0.42f, 0.38f, 1f)
            shapeRenderer.circle(asteroid.position.x, asteroid.position.y, asteroid.radius)
            shapeRenderer.color = Color(0.30f, 0.28f, 0.25f, 1f)
            shapeRenderer.circle(
                asteroid.position.x + asteroid.radius * 0.25f,
                asteroid.position.y - asteroid.radius * 0.2f,
                asteroid.radius * 0.35f
            )
        }
    }

    private fun showShopDialog() {
        if (shopOpen) return
        shopOpen = true
        isPaused = true

        val dialogSkin = Skin(Gdx.files.internal("uiskin.json"))
        val generator = FreeTypeFontGenerator(Gdx.files.internal("wheaton_capitals.otf"))

        // Scale font sizes relative to screen height so the dialog is readable on mobile.
        val titleFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (Gdx.graphics.height * 0.055f).toInt().coerceAtLeast(22)
            color = Color.WHITE
        })
        val bodyFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (Gdx.graphics.height * 0.038f).toInt().coerceAtLeast(15)
            color = Color.WHITE
        })
        val smallFont = generator.generateFont(FreeTypeFontGenerator.FreeTypeFontParameter().apply {
            size = (Gdx.graphics.height * 0.030f).toInt().coerceAtLeast(12)
            color = Color.WHITE
        })
        generator.dispose()

        val titleStyle = Label.LabelStyle(titleFont, Color.WHITE)
        val bodyStyle = Label.LabelStyle(bodyFont, Color.WHITE)
        val smallStyle = Label.LabelStyle(smallFont, Color.WHITE)
        val statStyle =
            Label.LabelStyle(smallFont, Color(0.75f, 1f, 0.75f, 1f)) // soft green for stat values
        val buttonStyle = com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle().apply {
            up = dialogSkin.getDrawable("default-round")
            down = dialogSkin.getDrawable("default-round-down")
            font = bodyFont
        }

        // Roll offer prices each time the shop opens or rerolls.
        fun rolledCost(base: Int): Int {
            val factor = (0.85f..1.15f).random()
            return ((base * factor) / 50f).toInt().coerceAtLeast(1) * 50
        }

        val rapidCost = rolledCost(1200)
        val engineCost = rolledCost(1000)
        val hullCost = rolledCost(1100)
        val repairCost = rolledCost(700)
        val rerollCost = rolledCost(450)

        val dialog = Dialog("", dialogSkin)

        fun closeShopDialog() {
            titleFont.dispose()
            bodyFont.dispose()
            smallFont.dispose()
            shopOpen = false
            isPaused = false
            dialog.hide()
        }

        fun applyUpgrade(option: String) {
            when (option) {
                "rapid" -> if (score >= rapidCost) {
                    score -= rapidCost
                    shotCooldownNanos =
                        (shotCooldownNanos * 0.85f).toLong().coerceAtLeast(160_000_000L)
                }

                "engine" -> if (score >= engineCost) {
                    score -= engineCost
                    moveSpeedMultiplier = (moveSpeedMultiplier + 0.15f).coerceAtMost(2.2f)
                }

                "hull" -> if (score >= hullCost) {
                    score -= hullCost
                    damageTakenMultiplier = (damageTakenMultiplier * 0.9f).coerceAtLeast(0.45f)
                }

                "repair" -> if (score >= repairCost) {
                    score -= repairCost
                    player.currentHealth =
                        (player.currentHealth + 35).coerceAtMost(player.maxHealth)
                }

                "reroll" -> if (score >= rerollCost) {
                    score -= rerollCost
                    closeShopDialog()
                    // Open a fresh dialog with newly rolled prices.
                    showShopDialog()
                    return
                }
            }
            closeShopDialog()
        }

        val btnWidth = Gdx.graphics.width * 0.33f
        val btnHeight = Gdx.graphics.height * 0.09f
        val pad = Gdx.graphics.height * 0.022f
        val statColW = Gdx.graphics.width * 0.28f

        // Derived readable stat values for the left stats panel.
        val fireRateMs = shotCooldownNanos / 1_000_000L
        val speedPct = (moveSpeedMultiplier * 100).toInt()
        val dmgReductPct = ((1f - damageTakenMultiplier) * 100).toInt().coerceAtLeast(0)
        val hp = player.currentHealth
        val maxHp = player.maxHealth

        // ── Stats panel (left column) ──────────────────────────────────────
        val statsTable = Table()
        statsTable.add(Label("SHIP STATS", titleStyle)).padBottom(pad * 0.8f).left().row()

        fun statRow(label: String, value: String) {
            statsTable.add(Label(label, smallStyle)).left().padRight(pad * 0.4f)
            statsTable.add(Label(value, statStyle)).right().row()
        }
        statRow("HP:", "$hp / $maxHp")
        statRow("Speed:", "${speedPct}%")
        statRow("Fire rate:", "$fireRateMs ms")
        statRow("Dmg reduc:", "-${dmgReductPct}%")

        // ── Shop panel (right column) ──────────────────────────────────────
        val shopTable = Table()
        shopTable.add(Label("MILESTONE SHOP", titleStyle)).padBottom(pad * 0.5f).colspan(2).center()
            .row()
        shopTable.add(Label("Score: $score", bodyStyle)).padBottom(pad * 0.8f).colspan(2).center()
            .row()

        fun upgradeBtn(label: String, preview: String, cost: Int, key: String) {
            val btn = com.badlogic.gdx.scenes.scene2d.ui.TextButton(
                "$label\n$preview\n(-$cost pts)",
                buttonStyle
            )
            btn.addListener(object : com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
                override fun clicked(
                    event: com.badlogic.gdx.scenes.scene2d.InputEvent?,
                    x: Float,
                    y: Float
                ) {
                    applyUpgrade(key)
                }
            })
            shopTable.add(btn).size(btnWidth, btnHeight).pad(pad * 0.4f)
        }

        val currentRapidMs = shotCooldownNanos / 1_000_000L
        val newRapidMs =
            (shotCooldownNanos * 0.85f).toLong().coerceAtLeast(160_000_000L) / 1_000_000L

        val currentSpeed = (moveSpeedMultiplier * 100).toInt()
        val newSpeed = ((moveSpeedMultiplier + 0.15f).coerceAtMost(2.2f) * 100).toInt()

        val currentReduc = ((1f - damageTakenMultiplier) * 100).toInt().coerceAtLeast(0)
        val newReduc = ((1f - (damageTakenMultiplier * 0.9f).coerceAtLeast(0.45f)) * 100).toInt()
            .coerceAtLeast(0)

        val currentHp = player.currentHealth
        val newHp = (player.currentHealth + 35).coerceAtMost(player.maxHealth)

        upgradeBtn("Rapid Fire", "${currentRapidMs}ms -> ${newRapidMs}ms", rapidCost, "rapid")
        upgradeBtn("Engine", "${currentSpeed}% -> ${newSpeed}%", engineCost, "engine")
        shopTable.row()
        upgradeBtn("Hull", "-${currentReduc}% -> -${newReduc}%", hullCost, "hull")
        upgradeBtn("Repair", "$currentHp -> $newHp HP", repairCost, "repair")
        shopTable.row()

        val rerollBtn = com.badlogic.gdx.scenes.scene2d.ui.TextButton(
            "Reroll Offers\n(-$rerollCost pts)",
            buttonStyle
        )
        rerollBtn.addListener(object : com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            override fun clicked(
                event: com.badlogic.gdx.scenes.scene2d.InputEvent?,
                x: Float,
                y: Float
            ) {
                applyUpgrade("reroll")
            }
        })

        val skipBtn = com.badlogic.gdx.scenes.scene2d.ui.TextButton("Saltar", buttonStyle)
        skipBtn.addListener(object : com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            override fun clicked(
                event: com.badlogic.gdx.scenes.scene2d.InputEvent?,
                x: Float,
                y: Float
            ) {
                applyUpgrade("skip")
            }
        })

        shopTable.add(rerollBtn).size(btnWidth, btnHeight).pad(pad).colspan(2).center().row()
        shopTable.add(skipBtn).size(btnWidth, btnHeight).pad(pad).colspan(2).center().row()

        // ── Assemble dialog ────────────────────────────────────────────────
        dialog.contentTable.pad(pad)
        dialog.contentTable.add(statsTable).width(statColW).top().padRight(pad * 1.5f)
        dialog.contentTable.add(shopTable).top()

        dialog.show(stage)
    }

    private data class Asteroid(
        val position: Vector2,
        val velocity: Vector2,
        val radius: Float,
        val damage: Int,
        var lifeSeconds: Float,
        var hp: Int = 40
    )

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

        // Draw circular badges for grouped markers.
        shapeRenderer.projectionMatrix = hudStage.camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.65f)
        for (marker in edgeMarkers) {
            if (marker.count <= 1) continue

            val countText = "x${marker.count}"
            markerCountLayout.setText(markerCountFont, countText)
            val badgeCenterX = marker.x
            val badgeCenterY = marker.y - 16f
            val radius = maxOf(10f, markerCountLayout.width * 0.5f + 6f)
            shapeRenderer.circle(badgeCenterX, badgeCenterY, radius)
        }
        shapeRenderer.end()

        // Draw group counts on top of badges.
        hudStage.batch.projectionMatrix = hudStage.camera.combined
        hudStage.batch.begin()
        for (marker in edgeMarkers) {
            if (marker.count <= 1) continue

            val countText = "x${marker.count}"
            markerCountLayout.setText(markerCountFont, countText)
            val textX = marker.x - markerCountLayout.width * 0.5f
            val textY = marker.y - 16f + markerCountLayout.height * 0.35f
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

    private fun drawIndicatorTriangle(
        markerX: Float,
        markerY: Float,
        centerX: Float,
        centerY: Float,
        size: Float
    ) {
        val dx = markerX - centerX
        val dy = markerY - centerY
        val lengthSquared = dx * dx + dy * dy
        if (lengthSquared < 0.0001f) return

        val invLength = 1f / sqrt(lengthSquared)
        val dirX = dx * invLength
        val dirY = dy * invLength
        val perpX = -dirY

        val baseDistance = size * 1.8f
        val halfWidth = size * 0.9f

        val baseX = markerX - dirX * baseDistance
        val baseY = markerY - dirY * baseDistance

        val leftX = baseX + perpX * halfWidth
        val leftY = baseY + dirX * halfWidth
        val rightX = baseX - perpX * halfWidth
        val rightY = baseY - dirX * halfWidth

        shapeRenderer.triangle(markerX, markerY, leftX, leftY, rightX, rightY)
    }

    private data class EdgeMarker(
        var x: Float,
        var y: Float,
        var enemyType: EnemyType,
        var count: Int
    )

    private fun drawJoysticks() {
        // Use a separate projection for screen-space UI
        val uiProjection = hudStage.camera.combined

        shapeRenderer.projectionMatrix = uiProjection

        // Enable blending for transparency
        Gdx.gl.glEnable(GL20.GL_BLEND)
        Gdx.gl.glBlendFunc(
            GL20.GL_SRC_ALPHA,
            GL20.GL_ONE_MINUS_SRC_ALPHA
        )

        // Render movement joystick (blue)
        joystickRenderer.render(shapeRenderer, movementTouchpad, isFireJoystick = false)

        // Render rotation/fire joystick (red)
        joystickRenderer.render(shapeRenderer, rotationTouchpad, isFireJoystick = true)

        Gdx.gl.glDisable(GL20.GL_BLEND)
    }
}
