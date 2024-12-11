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
import es.masmultimedia.entities.Projectile
import es.masmultimedia.entities.ProjectileFactory
import es.masmultimedia.entities.Spaceship
import es.masmultimedia.entities.Star
import es.masmultimedia.game.SimpleSurvivorGame
import es.masmultimedia.utils.GameAssetManager
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

        val touchpadStyle = Touchpad.TouchpadStyle().apply {
            background = skin.getDrawable("default-round")
            knob = skin.getDrawable("default-round")
        }

        movementTouchpad = Touchpad(10f, touchpadStyle).apply {
            setBounds(15f, 15f, 200f, 200f)
        }

        rotationTouchpad = Touchpad(10f, touchpadStyle).apply {
            setBounds(Gdx.graphics.width - 215f, 15f, 200f, 200f)
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

    override fun render(delta: Float) {
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
                        break
                    }
                }
            }
        }

        if (TimeUtils.nanoTime() - lastShotTime > 500_000_000L) {
            if (rotationTouchpad.isTouched) {
                val projectile = ProjectileFactory.createProjectile(
                    type = player.projectileType,
                    position = player.position.cpy(),
                    direction = lastPlayerDirection.cpy()
                )
                projectiles.add(projectile)
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
