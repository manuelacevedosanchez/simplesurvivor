package es.masmultimedia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.InputMultiplexer
import com.badlogic.gdx.InputProcessor
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.Texture
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
import es.masmultimedia.entities.Projectile
import es.masmultimedia.entities.Spaceship
import es.masmultimedia.game.SimpleSurvivorGame

class GameScreen(private val game: SimpleSurvivorGame) : Screen, InputProcessor {
    private lateinit var camera: OrthographicCamera
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var spriteBatch: SpriteBatch
    private lateinit var player: Spaceship
    private lateinit var enemyTexture: Texture

    private var playerSpeed = 200f
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
        player = Spaceship(Vector2(0f, 0f))
        enemyTexture = Texture("enemy_01.png")

        gameStartTime = TimeUtils.millis()
        lastEnemySpawnTime = TimeUtils.millis()

        // Configurar los joysticks virtuales
        stage = Stage()
        Gdx.input.inputProcessor = stage

        val skin = Skin(Gdx.files.internal("uiskin.json"))

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

        // Set up the InputMultiplexer
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
            // Draw the pause menu
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            stage.act(delta)
            stage.draw()
            return
        }

        // Limpiar la pantalla con un fondo negro (espacio)
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
            // Cálculo de la dirección del jugador
            val playerDirection = Vector2(moveX, moveY)
            if (playerDirection.len() > 0) {
                playerDirection.nor() // Normaliza la dirección

                // Calcula la nueva posición del jugador
                val newPosition =
                    player.position.cpy().add(playerDirection.scl(playerSpeed * delta))

                // Actualizar la posición del jugador sin restricciones
                player.position.set(newPosition)
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
            // Aumentar la dificultad reduciendo el intervalo
            if (enemySpawnInterval > 1000L) { // No bajar de 1 segundo
                enemySpawnInterval -= 100L // Reducir 100 ms cada vez
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
                enemyIterator.remove() // Remover al enemigo después de colisionar
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
                        score += 100 // Puedes ajustar el valor de puntuación
                        break
                    }
                }
            }
        }

        // Disparos automáticos
        if (TimeUtils.nanoTime() - lastShotTime > 500_000_000L) { // Disparar cada 0.5 segundos
            if (rotationTouchpad.isTouched) {
                projectiles.add(
                    Projectile(
                        player.position.cpy(),
                        lastPlayerDirection.cpy()
                    )
                ) // Disparar en la dirección de la rotación del jugador
            }
            lastShotTime = TimeUtils.nanoTime()
        }

        // Actualizar proyectiles
        val projectileIterator = projectiles.iterator()
        while (projectileIterator.hasNext()) {
            val projectile = projectileIterator.next()
            projectile.update()
            // Remover proyectiles si están muy lejos (opcional)
            if (projectile.position.dst(player.position) > 1000f) {
                projectileIterator.remove()
            }
        }

        // Dibujar jugador, enemigos y proyectiles
        spriteBatch.projectionMatrix = camera.combined
        spriteBatch.begin()
        player.render(spriteBatch)
        for (enemy in enemies) {
            spriteBatch.draw(enemyTexture, enemy.position.x, enemy.position.y, 20f, 20f)
        }
        spriteBatch.end()

        // Renderizar la barra de vida y los proyectiles con ShapeRenderer
        shapeRenderer.projectionMatrix = camera.combined
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Dibujar la barra de vida del jugador
        drawPlayerHealthBar()

        // Dibujar los proyectiles
        shapeRenderer.color = com.badlogic.gdx.graphics.Color.GREEN
        for (projectile in projectiles) {
            shapeRenderer.circle(projectile.position.x, projectile.position.y, 5f)
        }

        shapeRenderer.end()

        // Dibujar los joysticks
        stage.act(delta)
        stage.draw()
    }

    private fun spawnEnemy() {
        // Distancia mínima y máxima para generar enemigos
        val minSpawnDistance = 500f
        val maxSpawnDistance = 1000f

        // Generar un ángulo aleatorio
        val angle = Math.random() * 2 * Math.PI

        // Generar una distancia aleatoria dentro del rango
        val distance =
            minSpawnDistance + Math.random().toFloat() * (maxSpawnDistance - minSpawnDistance)

        // Calcular posición de spawn
        val spawnX = player.position.x + distance * Math.cos(angle).toFloat()
        val spawnY = player.position.y + distance * Math.sin(angle).toFloat()

        // Crear nuevo enemigo
        val newEnemy = Enemy(Vector2(spawnX, spawnY))

        enemies.add(newEnemy)
    }

    private fun drawPlayerHealthBar() {
        // Calcular el porcentaje de salud restante
        val healthPercentage = player.currentHealth.toFloat() / player.maxHealth.toFloat()

        // Interpolación de color de verde a rojo
        val healthColor = com.badlogic.gdx.graphics.Color(
            1 - healthPercentage, // Rojo aumenta a medida que disminuye la salud
            healthPercentage,     // Verde disminuye a medida que disminuye la salud
            0f,                   // Azul permanece en 0
            1f                    // Alpha
        )
        shapeRenderer.color = healthColor

        // Dimensiones de la barra de vida
        val barWidth = player.width
        val barHeight = 5f
        val barX = player.position.x - barWidth / 2
        val barY = player.position.y - player.height / 2 - barHeight - 5f // Debajo de la nave

        // Dibujar la barra de salud
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
        // Actualizar el viewport del stage
        stage.viewport.update(width, height, true)
    }

    override fun pause() {}

    override fun resume() {}

    override fun hide() {}

    override fun dispose() {
        // Liberar recursos
        player.dispose()
        spriteBatch.dispose()
        shapeRenderer.dispose()
        stage.dispose()
        enemyTexture.dispose()
    }

    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.BACK || keycode == Input.Keys.ESCAPE) {
            // Handle the "Atrás" button
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
