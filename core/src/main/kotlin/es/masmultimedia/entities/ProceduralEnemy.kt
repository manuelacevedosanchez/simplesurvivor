package es.masmultimedia.entities

import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Enemy shape types for procedural generation
 */
enum class EnemyShape {
    CIRCLE,      // Basic circular enemy
    TRIANGLE,    // Aggressive triangular enemy
    SQUARE,      // Tanky square enemy
    HEXAGON,     // Medium hexagonal enemy
    STAR,        // Special star-shaped enemy
    DIAMOND      // Fast diamond-shaped enemy
}

/**
 * Procedurally generated enemy that renders using ShapeRenderer instead of textures.
 * Each enemy type has a unique shape, color, and visual style.
 */
open class ProceduralEnemy(
    open val position: Vector2,
    open var health: Int,
    open val maxHealth: Int = health,
    open val speed: Float,
    open val type: EnemyType,
    open val shape: EnemyShape = EnemyShape.CIRCLE,
    open val size: Float = 20f,
    open val primaryColor: Color = Color.RED,
    open val secondaryColor: Color = Color.WHITE,
    open val glowColor: Color = Color.ORANGE
) {
    private val boundsRect = Rectangle()
    private var rotation = 0f
    private var pulsePhase = 0f
    private var damageFlashTime = 0f

    // Animation parameters
    private val rotationSpeed = when (type) {
        EnemyType.FAST -> 180f
        EnemyType.STRONG -> 30f
        EnemyType.NORMAL -> 90f
    }

    open val bounds: Rectangle
        get() = boundsRect.set(
            position.x - size,
            position.y - size,
            size * 2,
            size * 2
        )

    open fun moveTowards(target: Vector2, speedFactor: Float = 1f) {
        val dx = target.x - position.x
        val dy = target.y - position.y
        val distanceSquared = dx * dx + dy * dy
        if (distanceSquared == 0f) return

        val scale = speed * speedFactor * Gdx.graphics.deltaTime / sqrt(distanceSquared)
        position.x += dx * scale
        position.y += dy * scale
    }

    open fun update(delta: Float) {
        rotation += rotationSpeed * delta
        pulsePhase += delta * 3f
        if (damageFlashTime > 0) {
            damageFlashTime -= delta
        }
    }

    open fun takeDamage(damage: Int) {
        health -= damage
        damageFlashTime = 0.1f // Flash white for 100ms when hit
    }

    open fun isAlive(): Boolean = health > 0

    /**
     * Renders the enemy using ShapeRenderer.
     * Call this between shapeRenderer.begin() and shapeRenderer.end()
     */
    open fun render(shapeRenderer: ShapeRenderer) {
        val pulseScale = 1f + sin(pulsePhase) * 0.05f
        val currentSize = size * pulseScale

        // Determine color (flash white when damaged)
        val drawColor = if (damageFlashTime > 0) Color.WHITE else primaryColor

        shapeRenderer.color = drawColor

        when (shape) {
            EnemyShape.CIRCLE -> drawCircle(shapeRenderer, currentSize)
            EnemyShape.TRIANGLE -> drawTriangle(shapeRenderer, currentSize)
            EnemyShape.SQUARE -> drawSquare(shapeRenderer, currentSize)
            EnemyShape.HEXAGON -> drawHexagon(shapeRenderer, currentSize)
            EnemyShape.STAR -> drawStar(shapeRenderer, currentSize)
            EnemyShape.DIAMOND -> drawDiamond(shapeRenderer, currentSize)
        }

        // Draw health indicator (inner circle showing remaining health)
        val healthRatio = health.toFloat() / maxHealth
        if (healthRatio < 1f) {
            shapeRenderer.color = secondaryColor
            shapeRenderer.circle(position.x, position.y, currentSize * 0.3f * healthRatio)
        }
    }

    private fun drawCircle(shapeRenderer: ShapeRenderer, size: Float) {
        // Outer glow
        shapeRenderer.color = glowColor.cpy().apply { a = 0.3f }
        shapeRenderer.circle(position.x, position.y, size * 1.2f)
        // Main body
        shapeRenderer.color = primaryColor
        shapeRenderer.circle(position.x, position.y, size)
        // Inner core
        shapeRenderer.color = secondaryColor
        shapeRenderer.circle(position.x, position.y, size * 0.4f)
    }

    private fun drawTriangle(shapeRenderer: ShapeRenderer, size: Float) {
        val points = getPolygonPoints(3, size, rotation)

        // Outer glow
        shapeRenderer.color = glowColor.cpy().apply { a = 0.3f }
        val glowPoints = getPolygonPoints(3, size * 1.3f, rotation)
        shapeRenderer.triangle(
            glowPoints[0].x, glowPoints[0].y,
            glowPoints[1].x, glowPoints[1].y,
            glowPoints[2].x, glowPoints[2].y
        )

        // Main body
        shapeRenderer.color = primaryColor
        shapeRenderer.triangle(
            points[0].x, points[0].y,
            points[1].x, points[1].y,
            points[2].x, points[2].y
        )

        // Inner core
        shapeRenderer.color = secondaryColor
        shapeRenderer.circle(position.x, position.y, size * 0.25f)
    }

    private fun drawSquare(shapeRenderer: ShapeRenderer, size: Float) {
        val halfSize = size * 0.8f

        // Outer glow
        shapeRenderer.color = glowColor.cpy().apply { a = 0.3f }
        shapeRenderer.rect(
            position.x - halfSize * 1.2f, position.y - halfSize * 1.2f,
            halfSize * 1.2f, halfSize * 1.2f,
            halfSize * 2.4f, halfSize * 2.4f,
            1f, 1f, rotation
        )

        // Main body
        shapeRenderer.color = primaryColor
        shapeRenderer.rect(
            position.x - halfSize, position.y - halfSize,
            halfSize, halfSize,
            halfSize * 2, halfSize * 2,
            1f, 1f, rotation
        )

        // Inner cross pattern
        shapeRenderer.color = secondaryColor
        val innerSize = halfSize * 0.3f
        shapeRenderer.rect(
            position.x - innerSize, position.y - halfSize * 0.6f,
            innerSize, halfSize * 0.6f,
            innerSize * 2, halfSize * 1.2f,
            1f, 1f, rotation
        )
    }

    private fun drawHexagon(shapeRenderer: ShapeRenderer, size: Float) {
        val points = getPolygonPoints(6, size, rotation)

        // Draw as triangles from center
        shapeRenderer.color = primaryColor
        for (i in 0 until 6) {
            val next = (i + 1) % 6
            shapeRenderer.triangle(
                position.x, position.y,
                points[i].x, points[i].y,
                points[next].x, points[next].y
            )
        }

        // Inner hexagon
        shapeRenderer.color = secondaryColor
        val innerPoints = getPolygonPoints(6, size * 0.4f, rotation + 30f)
        for (i in 0 until 6) {
            val next = (i + 1) % 6
            shapeRenderer.triangle(
                position.x, position.y,
                innerPoints[i].x, innerPoints[i].y,
                innerPoints[next].x, innerPoints[next].y
            )
        }
    }

    private fun drawStar(shapeRenderer: ShapeRenderer, size: Float) {
        val outerPoints = getPolygonPoints(5, size, rotation)
        val innerPoints = getPolygonPoints(5, size * 0.5f, rotation + 36f)

        // Draw star shape as triangles
        shapeRenderer.color = primaryColor
        for (i in 0 until 5) {
            val nextOuter = (i + 1) % 5
            // Triangle from center to outer point
            shapeRenderer.triangle(
                position.x, position.y,
                outerPoints[i].x, outerPoints[i].y,
                innerPoints[i].x, innerPoints[i].y
            )
            shapeRenderer.triangle(
                position.x, position.y,
                innerPoints[i].x, innerPoints[i].y,
                outerPoints[nextOuter].x, outerPoints[nextOuter].y
            )
        }

        // Center circle
        shapeRenderer.color = secondaryColor
        shapeRenderer.circle(position.x, position.y, size * 0.2f)
    }

    private fun drawDiamond(shapeRenderer: ShapeRenderer, size: Float) {
        val points = getPolygonPoints(4, size, rotation + 45f)

        // Elongate vertically for diamond shape
        points[0].y = position.y + size * 1.5f  // Top
        points[2].y = position.y - size * 1.5f  // Bottom

        // Outer glow
        shapeRenderer.color = glowColor.cpy().apply { a = 0.4f }
        shapeRenderer.triangle(
            points[0].x, points[0].y,
            points[1].x, points[1].y,
            points[3].x, points[3].y
        )
        shapeRenderer.triangle(
            points[2].x, points[2].y,
            points[1].x, points[1].y,
            points[3].x, points[3].y
        )

        // Main body - scaled down
        val innerScale = 0.85f
        shapeRenderer.color = primaryColor
        shapeRenderer.triangle(
            position.x, position.y + size * 1.5f * innerScale,
            position.x + size * innerScale, position.y,
            position.x - size * innerScale, position.y
        )
        shapeRenderer.triangle(
            position.x, position.y - size * 1.5f * innerScale,
            position.x + size * innerScale, position.y,
            position.x - size * innerScale, position.y
        )

        // Center line
        shapeRenderer.color = secondaryColor
        shapeRenderer.rectLine(
            position.x, position.y - size * 0.8f,
            position.x, position.y + size * 0.8f,
            3f
        )
    }

    private fun getPolygonPoints(sides: Int, radius: Float, rotationDeg: Float): MutableList<Vector2> {
        val points = mutableListOf<Vector2>()
        val angleStep = 360f / sides
        val startAngle = rotationDeg - 90f // Start from top

        for (i in 0 until sides) {
            val angle = Math.toRadians((startAngle + i * angleStep).toDouble())
            points.add(Vector2(
                position.x + radius * cos(angle).toFloat(),
                position.y + radius * sin(angle).toFloat()
            ))
        }
        return points
    }

    // Legacy render method for SpriteBatch compatibility (does nothing)
    open fun render(batch: SpriteBatch) {
        // Procedural enemies use ShapeRenderer, not SpriteBatch
    }
}

