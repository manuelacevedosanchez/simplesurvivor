package es.masmultimedia.game

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
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
import com.badlogic.gdx.math.Vector3
import com.badlogic.gdx.utils.TimeUtils
import es.masmultimedia.entities.Enemy
import es.masmultimedia.entities.Projectile

class SimpleSurvivorGame : ApplicationAdapter() {
    private lateinit var camera: OrthographicCamera
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var spriteBatch: SpriteBatch
    private lateinit var tiledMap: TiledMap
    private lateinit var tiledMapRenderer: OrthogonalTiledMapRenderer
    private lateinit var playerPosition: Vector2
    private lateinit var enemy: Enemy

    private var playerSpeed = 200f

    private val playerRadius = 20f
    private val worldWidth = 1600f
    private val worldHeight = 1200f
    private val collisionRectangles = mutableListOf<Rectangle>()
    private val projectiles = mutableListOf<Projectile>()
    private var lastShotTime = 0L

    override fun create() {
        camera = OrthographicCamera().apply {
            setToOrtho(false, 800f, 600f)
            position.set(worldWidth / 2, worldHeight / 2, 0f)
        }

        // Cargar el mapa de Tiled
        tiledMap = TmxMapLoader().load("level1.tmx")
        tiledMapRenderer = OrthogonalTiledMapRenderer(tiledMap, 1f)

        shapeRenderer = ShapeRenderer()
        spriteBatch = SpriteBatch()
        playerPosition = Vector2(worldWidth / 2, worldHeight / 2)
        enemy = Enemy(Vector2(100f, 100f))

        // Cargar los objetos de colisión desde la capa de objetos "Collisions"
        val collisionLayer = tiledMap.layers.get("Collisions")
        if (collisionLayer != null) {
            for (mapObject in collisionLayer.objects) {
                if (mapObject is RectangleMapObject) {
                    val rectangle = mapObject.rectangle

                    // Verificar si el objeto tiene el atributo 'collidable' y si es true
                    val collidable = mapObject.properties["collidable"]?.let { it as? Boolean } ?: true
                    if (collidable) {
                        collisionRectangles.add(rectangle)
                        Gdx.app.log("CollisionObject", "Added collision object at (${rectangle.x}, ${rectangle.y}) with size (${rectangle.width} x ${rectangle.height})")
                    }
                }
            }
        } else {
            Gdx.app.log("CollisionLayer", "No collision layer found")
        }
    }

    override fun render() {
        // Limpiar la pantalla
        Gdx.gl.glClearColor(0.9f, 0.9f, 0.9f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Actualizar la posición de la cámara para mantener al jugador en el centro
        camera.position.set(playerPosition.x, playerPosition.y, 0f)
        camera.update()

        // Dibujar el mapa de Tiled
        tiledMapRenderer.setView(camera)
        tiledMapRenderer.render()

        // Movimiento del jugador basado en la dirección de la entrada táctil
        if (Gdx.input.isTouched) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.input.y.toFloat()
            val touchPos = camera.unproject(Vector3(touchX, touchY, 0f))
            val direction = Vector2(touchPos.x - playerPosition.x, touchPos.y - playerPosition.y).nor()
            val newPosition = playerPosition.cpy().add(direction.scl(playerSpeed * Gdx.graphics.deltaTime))

            // Comprobar colisiones con los objetos del mapa de Tiled
            var collision = false
            for (rectangle in collisionRectangles) {
                if (rectangle.contains(newPosition.x, newPosition.y)) {
                    collision = true
                    Gdx.app.log("Collision", "Collision detected with object at (${rectangle.x}, ${rectangle.y})")
                    break
                }
            }

            // Actualizar la posición del jugador solo si no hay colisión
            if (!collision) {
                playerPosition.set(newPosition)
            }
        }

        shapeRenderer.projectionMatrix = camera.combined

        // Movimiento del enemigo hacia el jugador
        enemy.moveTowards(playerPosition)

        // Disparos automáticos
        if (TimeUtils.nanoTime() - lastShotTime > 500_000_000L) { // Disparar cada 0.5 segundos
            projectiles.add(Projectile(playerPosition.cpy(), Vector2(1f, 0f))) // Disparar hacia la derecha por defecto
            lastShotTime = TimeUtils.nanoTime()
        }

        // Actualizar proyectiles
        val projectileIterator = projectiles.iterator()
        while (projectileIterator.hasNext()) {
            val projectile = projectileIterator.next()
            projectile.update()
            // Remover proyectiles fuera del mundo
            if (projectile.position.x > worldWidth || projectile.position.x < 0 ||
                projectile.position.y > worldHeight || projectile.position.y < 0) {
                projectileIterator.remove()
            }
        }

        // Dibujar jugador, enemigo y proyectiles
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)

        // Dibujar el jugador
        shapeRenderer.color = com.badlogic.gdx.graphics.Color.BLUE
        shapeRenderer.circle(playerPosition.x, playerPosition.y, playerRadius)

        // Dibujar el enemigo
        shapeRenderer.color = com.badlogic.gdx.graphics.Color.RED
        shapeRenderer.rect(enemy.position.x, enemy.position.y, 20f, 20f)

        // Dibujar proyectiles
        shapeRenderer.color = com.badlogic.gdx.graphics.Color.GREEN
        for (projectile in projectiles) {
            shapeRenderer.circle(projectile.position.x, projectile.position.y, 5f)
        }

        shapeRenderer.end()
    }

    override fun dispose() {
        shapeRenderer.dispose()
        spriteBatch.dispose()
        tiledMap.dispose()
    }
}
