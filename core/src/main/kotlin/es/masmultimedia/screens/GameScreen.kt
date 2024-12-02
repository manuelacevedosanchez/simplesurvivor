package es.masmultimedia.screens

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Screen
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.maps.objects.RectangleMapObject
import com.badlogic.gdx.maps.tiled.TiledMap
import com.badlogic.gdx.maps.tiled.TmxMapLoader
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import com.badlogic.gdx.utils.TimeUtils
import es.masmultimedia.entities.Enemy
import es.masmultimedia.entities.Projectile
import es.masmultimedia.entities.Spaceship
import es.masmultimedia.game.SimpleSurvivorGame

class GameScreen(private val game: SimpleSurvivorGame) : Screen {
    private lateinit var camera: OrthographicCamera
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var spriteBatch: SpriteBatch
    private lateinit var tiledMap: TiledMap
    private lateinit var tiledMapRenderer: OrthogonalTiledMapRenderer
    private lateinit var player: Spaceship
    private lateinit var enemy: Enemy

    private var playerSpeed = 200f
    private var gameStartTime = 0L
    private var gameEnded = false
    private var gameWon = false

    private val worldWidth = 1600f
    private val worldHeight = 1200f
    private val collisionRectangles = mutableListOf<Rectangle>()
    private val projectiles = mutableListOf<Projectile>()
    private var lastShotTime = 0L
    private var lastPlayerDirection = Vector2(1f, 0f) // Default direction to the right

    private lateinit var stage: Stage
    private lateinit var movementTouchpad: Touchpad
    private lateinit var rotationTouchpad: Touchpad

    override fun show() {
        camera = OrthographicCamera().apply {
            setToOrtho(false, 800f, 600f)
            position.set(worldWidth / 2, worldHeight / 2, 0f)
        }

        // Cargar el mapa de Tiled
        tiledMap = TmxMapLoader().load("level1.tmx")
        tiledMapRenderer = OrthogonalTiledMapRenderer(tiledMap, 1f)

        shapeRenderer = ShapeRenderer()
        spriteBatch = SpriteBatch()
// Obtener el tamaño del mapa en píxeles
        val mapWidth =
            tiledMap.properties["width"] as Int * (tiledMap.properties["tilewidth"] as Int)
        val mapHeight =
            tiledMap.properties["height"] as Int * (tiledMap.properties["tileheight"] as Int)

// Centrar al jugador en el mapa
        player = Spaceship(Vector2(mapWidth / 2f, mapHeight / 2f))
        enemy = Enemy(Vector2(100f, 100f))

        // Cargar los objetos de colisión desde la capa de objetos "Collisions"
        val collisionLayer = tiledMap.layers.get("Collisions")
        if (collisionLayer != null) {
            for (mapObject in collisionLayer.objects) {
                if (mapObject is RectangleMapObject) {
                    val rectangle = mapObject.rectangle

                    // Verificar si el objeto tiene el atributo 'collidable' y si es true
                    val collidable =
                        mapObject.properties["collidable"]?.let { it as? Boolean } ?: true
                    if (collidable) {
                        collisionRectangles.add(rectangle)
                        Gdx.app.log(
                            "CollisionObject",
                            "Added collision object at (${rectangle.x}, ${rectangle.y}) with size (${rectangle.width} x ${rectangle.height})"
                        )
                    }
                }
            }
        } else {
            Gdx.app.log("CollisionLayer", "No collision layer found")
        }

        gameStartTime = TimeUtils.millis()

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
    }

    override fun render(delta: Float) {
        if (gameEnded) {
            // Mostrar mensaje de fin del juego
            Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
            shapeRenderer.color =
                if (gameWon) com.badlogic.gdx.graphics.Color.GREEN else com.badlogic.gdx.graphics.Color.RED
            shapeRenderer.circle(400f, 300f, 100f)
            shapeRenderer.end()
            return
        }

        // Limpiar la pantalla
        Gdx.gl.glClearColor(0.9f, 0.9f, 0.9f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Verificar condición de victoria
        if (TimeUtils.timeSinceMillis(gameStartTime) > 120000) { // 2 minutos
            gameEnded = true
            gameWon = true
            return
        }

        // Actualizar la posición de la cámara para mantener al jugador en el centro
        camera.position.set(player.position.x, player.position.y, 0f)
        camera.update()

        // Dibujar el mapa de Tiled
        tiledMapRenderer.setView(camera)
        tiledMapRenderer.render()

        // Movimiento del jugador basado en el joystick de movimiento
        val moveX = movementTouchpad.knobPercentX
        val moveY = movementTouchpad.knobPercentY

// Debug para verificar los valores del joystick
        Gdx.app.log("Joystick Movimiento", "knobPercentX: $moveX, knobPercentY: $moveY")

        if (movementTouchpad.isTouched) {
            // Cálculo de la dirección del jugador
            val playerDirection = Vector2(moveX, moveY)
            if (playerDirection.len() > 0) {
                playerDirection.nor() // Normaliza la dirección
                Gdx.app.log(
                    "PlayerDirection",
                    "Direction: ${playerDirection.x}, ${playerDirection.y}"
                )

                // Calcula la nueva posición del jugador
                val newPosition =
                    player.position.cpy().add(playerDirection.scl(playerSpeed * delta))
                // player.position.set(newPosition) // Actualiza la posición
                Gdx.app.log(
                    "PlayerPosition",
                    "Position: ${player.position.x}, ${player.position.y}"
                )

                // Limitar la posición para que no se salga del área del juego
                newPosition.x = newPosition.x.coerceIn(0f, worldWidth - player.width)
                newPosition.y = newPosition.y.coerceIn(0f, worldHeight - player.height)

                // Comprobar colisiones con los objetos del mapa de Tiled
                var collision = false
                for (rectangle in collisionRectangles) {
                    if (rectangle.overlaps(
                            Rectangle(
                                newPosition.x - player.width / 2,
                                newPosition.y - player.height / 2,
                                player.width,
                                player.height
                            )
                        )
                    ) {
                        collision = true
                        Gdx.app.log(
                            "Collision",
                            "Collision detected with object at (${rectangle.x}, ${rectangle.y})"
                        )
                        break
                    }
                }

                // Actualizar la posición del jugador solo si no hay colisión
                if (!collision) {
                    player.position.set(newPosition)
                    Gdx.app.log(
                        "PlayerPosition",
                        "Position: ${player.position.x}, ${player.position.y}"
                    )
                }
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

        // Movimiento del enemigo hacia el jugador
        enemy.moveTowards(player.position)

        // Verificar colisión del enemigo con el jugador (condición de derrota)
        if (enemy.bounds.contains(player.position)) {
            gameEnded = true
            gameWon = false
            return
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
            // Remover proyectiles fuera del mundo
            if (projectile.position.x > worldWidth || projectile.position.x < 0 ||
                projectile.position.y > worldHeight || projectile.position.y < 0
            ) {
                projectileIterator.remove()
            }

            // Comprobar colisiones con el enemigo
            if (enemy.bounds.contains(projectile.position)) {
                enemy.takeDamage(projectile.power)
                projectileIterator.remove()
            }
        }

        // Dibujar jugador, enemigo y proyectiles
        spriteBatch.projectionMatrix = camera.combined
        spriteBatch.begin()
        player.render(spriteBatch)
        spriteBatch.end()

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Dibujar el enemigo si está vivo
        if (enemy.isAlive()) {
            shapeRenderer.color = com.badlogic.gdx.graphics.Color.RED
            shapeRenderer.rect(enemy.position.x, enemy.position.y, 20f, 20f)
        }

        // Dibujar proyectiles
        shapeRenderer.color = com.badlogic.gdx.graphics.Color.GREEN
        for (projectile in projectiles) {
            shapeRenderer.circle(projectile.position.x, projectile.position.y, 5f)
        }

        shapeRenderer.end()

        // Dibujar los joysticks
        stage.act(delta)
        stage.draw()
    }

    override fun resize(width: Int, height: Int) {}

    override fun pause() {}

    override fun resume() {}

    override fun hide() {}

    override fun dispose() {
        // Dispose para liberar recursos
        player.dispose()
        spriteBatch.dispose()
        tiledMap.dispose()
        shapeRenderer.dispose()
        stage.dispose()
    }
}
