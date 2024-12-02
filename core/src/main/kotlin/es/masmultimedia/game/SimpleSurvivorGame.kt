import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.math.Vector3
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

class SimpleSurvivorGame : ApplicationAdapter() {
    private lateinit var camera: OrthographicCamera
    private lateinit var shapeRenderer: ShapeRenderer
    private lateinit var playerPosition: Vector2
    private lateinit var enemyPosition: Vector2
    private var playerSpeed = 200f
    private val worldWidth = 1600f
    private val worldHeight = 1200f

    override fun create() {
        camera = OrthographicCamera().apply {
            setToOrtho(false, 800f, 600f)
            position.set(worldWidth / 2, worldHeight / 2, 0f)
        }
        shapeRenderer = ShapeRenderer()
        playerPosition = Vector2(worldWidth / 2, worldHeight / 2)
        enemyPosition = Vector2(100f, 100f)
    }

    override fun render() {
        // Limpiar la pantalla
        Gdx.gl.glClearColor(0.9f, 0.9f, 0.9f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        // Movimiento del jugador basado en la dirección de la entrada táctil
        if (Gdx.input.isTouched) {
            val touchX = Gdx.input.x.toFloat()
            val touchY = Gdx.input.y.toFloat()
            val touchPos = camera.unproject(Vector3(touchX, touchY, 0f))
            val direction = Vector2(touchPos.x - playerPosition.x, touchPos.y - playerPosition.y).nor()
            playerPosition.add(direction.scl(playerSpeed * Gdx.graphics.deltaTime))
        }

        // Actualizar la posición de la cámara para mantener al jugador en el centro
        camera.position.set(playerPosition.x, playerPosition.y, 0f)
        camera.update()
        shapeRenderer.projectionMatrix = camera.combined

        // Movimiento del enemigo hacia el jugador
        val direction = Vector2(playerPosition.x - enemyPosition.x, playerPosition.y - enemyPosition.y)
        direction.nor()
        enemyPosition.add(direction.scl(100 * Gdx.graphics.deltaTime))

        // Dibujar jugador y enemigo
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = com.badlogic.gdx.graphics.Color.BLUE
        shapeRenderer.circle(playerPosition.x, playerPosition.y, 20f)

        shapeRenderer.color = com.badlogic.gdx.graphics.Color.RED
        shapeRenderer.rect(enemyPosition.x, enemyPosition.y, 20f, 20f)
        shapeRenderer.end()
    }

    override fun dispose() {
        shapeRenderer.dispose()
    }
}
