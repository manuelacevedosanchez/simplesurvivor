package es.masmultimedia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Dialog
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.TimeUtils
import es.masmultimedia.entities.*
import es.masmultimedia.game.SimpleSurvivorGame
import es.masmultimedia.utils.GameAssetManager

class GameScreen(private val game: SimpleSurvivorGame) : Screen, InputProcessor {
    private lateinit var camera: OrthographicCamera
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var spriteBatch: SpriteBatch
    private lateinit var player: Spaceship
    lateinit var font: BitmapFont

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

    override fun show() {
        camera = OrthographicCamera().apply {
            setToOrtho(false, 800f, 600f)
            position.set(0f, 0f, 0f)
        }

        shapeRenderer = ShapeRenderer()
        spriteBatch = SpriteBatch()

        // Inicializar al jugador en el origen
        player = Spaceship(
            position = Vector2(0f, 0f),
            texture = GameAssetManager.getTexture("spaceship_base.png")
        )

        gameStartTime = TimeUtils.millis()
        lastEnemySpawnTime = TimeUtils.millis()

        // Configurar los joysticks virtuales
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

        // Configurar el InputMultiplexer
        val inputMultiplexer = InputMultiplexer(this, stage)
        Gdx.input.inputProcessor = inputMultiplexer
    }

    override fun render(delta: Float) {
        if (gameEnded) {
            // Transicionar a GameOverScreen
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
            // Dibujar el menú de pausa
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            stage.act(delta)
            stage.draw()
            return
        }

        // Limpiar la pantalla con un fondo negro
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Verificar condición de victoria
        if (TimeUtils.timeSinceMillis(gameStartTime) > 120000) { // 2 minutos
            gameEnded = true
            gameWon = true
            return
        }

        // Actualizar la posición de la cámara para seguir al jugador
        camera.position.set(player.position.x, player.position.y, 0f)
        camera.update()

        // Movimiento del jugador basado en el joystick de movimiento
        val moveX = movementTouchpad.knobPercentX
        val moveY = movementTouchpad.knobPercentY

        if (movementTouchpad.isTouched) {
            val playerDirection = Vector2(moveX, moveY)
            if (playerDirection.len() > 0) {
                playerDirection.nor()
                player.updatePosition(playerDirection, Gdx.graphics.deltaTime)
            }
        }

        // Rotación del jugador basada en el joystick de rotación
        val rotX = rotationTouchpad.knobPercentX
        val rotY = rotationTouchpad.knobPercentY
        if (rotationTouchpad.isTouched && (rotX != 0f || rotY != 0f)) {
            val rotationDirection = Vector2(rotX, rotY).nor()
            player.rotation = rotationDirection.angleDeg() - 90
            lastPlayerDirection = rotationDirection
        }

        shapeRenderer.projectionMatrix = camera.combined

        // Generar enemigos a intervalos regulares
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

            // Verificar colisión con el jugador
            if (enemy.bounds.overlaps(
                    Rectangle(
                        player.position.x - player.width / 2,
                        player.position.y - player.height / 2,
                        player.width,
                        player.height
                    )
                )
            ) {
                player.takeDamage(20) // Ajusta el valor de daño según sea necesario
                enemyIterator.remove()
                if (!player.isAlive()) {
                    gameEnded = true
                    gameWon = false
                    return
                }
                continue
            }

            // Verificar colisiones con proyectiles
            val projectileIterator = projectiles.iterator()
            while (projectileIterator.hasNext()) {
                val projectile = projectileIterator.next()
                if (enemy.bounds.contains(projectile.position)) {
                    enemy.takeDamage(projectile.power)
                    projectileIterator.remove()
                    if (!enemy.isAlive()) {
                        enemyIterator.remove()
                        enemiesDefeated++
                        score += 100 // Ajusta el valor de puntuación según sea necesario
                        break
                    }
                }
            }
        }

        // Disparos automáticos
        if (TimeUtils.nanoTime() - lastShotTime > 500_000_000L) { // Disparar cada 0.5 segundos
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

        // Actualizar proyectiles
        val projectileIterator = projectiles.iterator()
        while (projectileIterator.hasNext()) {
            val projectile = projectileIterator.next()
            projectile.update()
            // Remover proyectiles si están muy lejos
            if (projectile.position.dst(player.position) > 1000f) {
                projectileIterator.remove()
            }
        }

        // Dibujar jugador, enemigos y proyectiles
        spriteBatch.projectionMatrix = camera.combined
        spriteBatch.begin()
        player.render(spriteBatch)
        for (enemy in enemies) {
            enemy.render(spriteBatch)
        }
        for (projectile in projectiles) {
            projectile.render(spriteBatch)
        }
        spriteBatch.end()

        // Renderizar la barra de vida del jugador con ShapeRenderer
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        drawPlayerHealthBar()
        shapeRenderer.end()

        // Dibujar los joysticks
        stage.act(delta)
        stage.draw()
    }

    private fun spawnEnemy() {
        val minSpawnDistance = 500f
        val maxSpawnDistance = 1000f

        val angle = Math.random() * 2 * Math.PI
        val distance =
            minSpawnDistance + Math.random().toFloat() * (maxSpawnDistance - minSpawnDistance)

        val spawnX = player.position.x + distance * Math.cos(angle).toFloat()
        val spawnY = player.position.y + distance * Math.sin(angle).toFloat()

        // Elegir un tipo de enemigo aleatorio
        val enemyType = getRandomEnemyType()

        // Crear nuevo enemigo usando la fábrica
        val newEnemy = EnemyFactory.createEnemy(enemyType, Vector2(spawnX, spawnY))

        enemies.add(newEnemy)
    }

    private fun getRandomEnemyType(): EnemyType {
        val values = EnemyType.values()
        return values.random()
    }

    private fun drawPlayerHealthBar() {
        val healthPercentage = player.currentHealth.toFloat() / player.maxHealth.toFloat()
        val healthColor = com.badlogic.gdx.graphics.Color(
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
        // No es necesario disponer las texturas aquí, ya que las gestiona GameAssetManager
    }

    // Implementación de InputProcessor
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
