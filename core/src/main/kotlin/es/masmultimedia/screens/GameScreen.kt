package es.masmultimedia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.TimeUtils
import es.masmultimedia.entities.Enemy
import es.masmultimedia.entities.EnemyFactory
import es.masmultimedia.entities.EnemyType
import es.masmultimedia.entities.PowerUp
import es.masmultimedia.entities.Projectile
import es.masmultimedia.entities.ProjectileFactory
import es.masmultimedia.entities.Spaceship
import es.masmultimedia.entities.Star
import es.masmultimedia.game.SimpleSurvivorGame
import es.masmultimedia.utils.GameAssetManager
import ktx.math.random
import kotlin.math.cos
import kotlin.math.sin

class GameScreen(private val game: SimpleSurvivorGame) : Screen, InputProcessor {
    private lateinit var camera: OrthographicCamera
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var spriteBatch: SpriteBatch

    private lateinit var player: Spaceship

    private var gameStartTime = 0L
    private var gameEnded = false
    private var gameWon = false
    private var enemiesDefeated = 0
    private var score = 0
    private var isPaused = false

    private val enemies = mutableListOf<Enemy>()
    private val projectiles = mutableListOf<Projectile>()
    private val skin = Skin(Gdx.files.internal("uiskin.json"))

    private val powerUps = mutableListOf<PowerUp>()
    private var lastPowerUpSpawnTime = 0L
    private var powerUpSpawnInterval = 10000L // cada 10 segundos

    private var lastShotTime = 0L
    private var lastEnemySpawnTime = 0L
    private var enemySpawnInterval = 5000L // Intervalo inicial de 5 segundos
    private var lastPlayerDirection = Vector2(1f, 0f) // Dirección por defecto hacia la derecha

    private lateinit var stage: Stage
    private lateinit var movementTouchpad: Touchpad
    private lateinit var rotationTouchpad: Touchpad

    // Capas de estrellas para parallax
    private val starsFar = mutableListOf<Star>()
    private val starsMid = mutableListOf<Star>()
    private val starsNear = mutableListOf<Star>()

    private val sectorWidth = 10000f
    private val sectorHeight = 10000f

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

        // Define un porcentaje para el margen
        val marginPercentage = 0.10f // 5% del tamaño de la pantalla
        val marginX = screenWidth * marginPercentage
        val marginY = screenHeight * marginPercentage

        val touchpadStyle = Touchpad.TouchpadStyle().apply {
            background = skin.getDrawable("default-round")
            knob = skin.getDrawable("default-round")
        }

        val touchpadSize = screenWidth * 0.10f // 25% del ancho, por ejemplo

        // Ejemplo: Touchpad de movimiento, en la esquina inferior izquierda
        // Lo situamos con un margenX de la izquierda y un marginY de la parte inferior
        movementTouchpad = Touchpad(10f, touchpadStyle).apply {
            setBounds(marginX, marginY, touchpadSize, touchpadSize)
        }

        // Ejemplo: Touchpad de rotación, en la esquina inferior derecha
        // Restamos 200f (ancho del touchpad) más el margen
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

        generateStars()
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

        // Dibujar un mosaico 3x3 alrededor de la cámara
        // Esto significa: la baldosa original y las 8 adyacentes:
        // dx, dy ∈ {-1, 0, 1}
        for (star in layer) {
            for (ix in -1..1) {
                for (iy in -1..1) {
                    // Calculamos la posición de la estrella en esta "baldosa" repetida
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
                TimeUtils.timeSinceMillis(gameStartTime)
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

        if (TimeUtils.timeSinceMillis(gameStartTime) > 120000) {
            gameEnded = true
            gameWon = true
            return
        }

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

        if (TimeUtils.timeSinceMillis(lastEnemySpawnTime) > enemySpawnInterval) {
            spawnEnemy()
            lastEnemySpawnTime = TimeUtils.millis()
            if (enemySpawnInterval > 1000L) {
                enemySpawnInterval -= 100L
            }
        }

        // Actualizar enemigos
        val enemyIterator = enemies.iterator()
        while (enemyIterator.hasNext()) {
            val enemy = enemyIterator.next()
            enemy.moveTowards(player.position)

            if (enemy.bounds.overlaps(
                    Rectangle(
                        player.position.x - player.width / 2,
                        player.position.y - player.height / 2,
                        player.width,
                        player.height
                    )
                )
            ) {
                player.takeDamage(20)
                enemyIterator.remove()
                if (!player.isAlive()) {
                    gameEnded = true
                    gameWon = false
                    return
                }
                continue
            }

            val projectileIterator = projectiles.iterator()
            while (projectileIterator.hasNext()) {
                val projectile = projectileIterator.next()
                if (enemy.bounds.contains(projectile.position)) {
                    enemy.takeDamage(projectile.power)
                    projectileIterator.remove()
                    if (!enemy.isAlive()) {
                        enemyIterator.remove()
                        enemiesDefeated++
                        score += 100

                        // Probabilidad según tipo de enemigo
                        val dropChance = when (enemy.type) {
                            EnemyType.NORMAL -> 0.9
                            EnemyType.FAST -> 0.9
                            EnemyType.STRONG -> 0.9
                            else -> 0.0
                        }

                        if (Math.random() < dropChance) {

                            val type = when (enemy.type) {
                                EnemyType.NORMAL -> PowerUp.Type.CHARGED_SHOT
                                EnemyType.FAST -> PowerUp.Type.TRIPLE_SHOT
                                EnemyType.STRONG -> PowerUp.Type.SHIELD
                                else -> PowerUp.Type.HEALTH // por defecto
                            }

                            val color = when (type) {
                                PowerUp.Type.HEALTH -> Color.GREEN
                                PowerUp.Type.TRIPLE_SHOT -> Color.RED
                                PowerUp.Type.SHIELD -> Color.CYAN
                                PowerUp.Type.CHARGED_SHOT -> Color.YELLOW
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

                        break
                    }
                }
            }
        }

        if (TimeUtils.nanoTime() - lastShotTime > 500_000_000L) { // Disparo cada 0.5 seg
            if (rotationTouchpad.isTouched) {
                if (!player.isTripleShotActive()) {
                    // Disparo normal
                    val projectile = ProjectileFactory.createProjectile(
                        type = player.projectileType,
                        position = player.position.cpy(),
                        direction = lastPlayerDirection.cpy()
                    )
                    projectiles.add(projectile)
                } else {
                    // Disparo triple
                    // 1) Disparo central
                    val pCenter = ProjectileFactory.createProjectile(
                        type = player.projectileType,
                        position = player.position.cpy(),
                        direction = lastPlayerDirection.cpy()
                    )

                    // 2) Disparo izquierdo (rotamos -10 grados por ejemplo)
                    val dirLeft = lastPlayerDirection.cpy().rotateDeg(-10f)
                    val pLeft = ProjectileFactory.createProjectile(
                        type = player.projectileType,
                        position = player.position.cpy(),
                        direction = dirLeft
                    )

                    // 3) Disparo derecho (rotamos +10 grados)
                    val dirRight = lastPlayerDirection.cpy().rotateDeg(10f)
                    val pRight = ProjectileFactory.createProjectile(
                        type = player.projectileType,
                        position = player.position.cpy(),
                        direction = dirRight
                    )

                    // Añadir los tres disparos
                    projectiles.addAll(listOf(pCenter, pLeft, pRight))
                }
            }
            lastShotTime = TimeUtils.nanoTime()
        }

        val projectileIterator = projectiles.iterator()
        while (projectileIterator.hasNext()) {
            val projectile = projectileIterator.next()
            projectile.update()
            if (projectile.position.dst(player.position) > 1000f) {
                projectileIterator.remove()
            }
        }

        if (TimeUtils.timeSinceMillis(lastPowerUpSpawnTime) > powerUpSpawnInterval) {
            val chance = Math.random()
            if (chance < 0.10) { // 10% de probabilidad
                spawnPowerUp()
            }
            lastPowerUpSpawnTime = TimeUtils.millis()
        }

        for (powerUp in powerUps) {
            powerUp.update(delta)
        }

        val powerUpIterator = powerUps.iterator()
        while (powerUpIterator.hasNext()) {
            val pu = powerUpIterator.next()
            if (pu.overlapsWith(player)) {
                // El jugador lo recogió
                powerUpIterator.remove()
                player.applyPowerUp(pu)
            } else if (pu.isExpired()) {
                powerUpIterator.remove() // desaparece tras 10s
            }

        }

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
// ... starfield ...
        for (pu in powerUps) {
            pu.render(shapeRenderer)
        }
// ...
        shapeRenderer.end()

        // Primero actualizar las estrellas y dibujarlas
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
        spriteBatch.end()

        // Dibujar el círculo del escudo si está activo
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
    }

    private fun updateStars(delta: Float) {
        updateLayer(starsFar, delta, 0.1f)
        updateLayer(starsMid, delta, 0.5f)
        updateLayer(starsNear, delta, 1.0f)
    }

    private fun updateLayer(layer: MutableList<Star>, delta: Float, factor: Float) {
        val twinkleSpeed = 3.0f

        for (star in layer) {
            // Parpadeo
            star.color.a += star.twinkleDirection * twinkleSpeed * delta
            if (star.color.a > 1f) {
                star.color.a = 1f
                star.twinkleDirection = -1
            } else if (star.color.a < 0.05f) {
                star.color.a = 0.05f
                star.twinkleDirection = 1
            }

            // Wrap-around global usando modulo
            // Función auxiliar para hacer wrap-around
            star.x = wrap(star.x, sectorWidth)
            star.y = wrap(star.y, sectorHeight)
        }
    }

    // Función wrap auxiliar
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

    private fun spawnEnemy() {
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
    }

    override fun resize(width: Int, height: Int) {
        stage.viewport.update(width, height, true)
    }

    override fun pause() {}
    override fun resume() {}
    override fun hide() {}
    override fun dispose() {
        player.dispose()
        spriteBatch.dispose()
        shapeRenderer.dispose()
        stage.dispose()
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
