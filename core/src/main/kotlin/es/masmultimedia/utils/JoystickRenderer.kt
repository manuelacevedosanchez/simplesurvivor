package es.masmultimedia.utils

import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.scenes.scene2d.ui.Touchpad
import kotlin.math.cos
import kotlin.math.sin

/**
 * Renders stylized joysticks with gradients, borders, and 3D effects.
 */
class JoystickRenderer {

    // Nearly transparent base to avoid blocking visual area
    private val baseColorOuter = Color(0.15f, 0.15f, 0.2f, 0.12f)
    private val baseColorInner = Color(0.25f, 0.25f, 0.35f, 0.08f)
    private val baseBorderColor = Color(0.5f, 0.5f, 0.6f, 0.2f)
    private val guideCircleColor = Color(0.4f, 0.4f, 0.5f, 0.1f)

    private val knobColorMove = Color(0.3f, 0.5f, 0.9f, 0.85f)
    private val knobHighlightMove = Color(0.5f, 0.7f, 1f, 0.95f)
    private val knobShadowMove = Color(0.15f, 0.25f, 0.5f, 0.9f)

    private val knobColorFire = Color(0.9f, 0.35f, 0.3f, 0.85f)
    private val knobHighlightFire = Color(1f, 0.55f, 0.5f, 0.95f)
    private val knobShadowFire = Color(0.5f, 0.15f, 0.12f, 0.9f)

    /**
     * Renders the joystick base and knob.
     * @param shapeRenderer Must NOT be inside begin/end when calling this.
     * @param touchpad The touchpad to render.
     * @param isFireJoystick True for red (fire/rotation), false for blue (movement).
     */
    fun render(shapeRenderer: ShapeRenderer, touchpad: Touchpad, isFireJoystick: Boolean) {
        val centerX = touchpad.x + touchpad.width / 2f
        val centerY = touchpad.y + touchpad.height / 2f
        val baseRadius = touchpad.width / 2f
        val knobRadius = baseRadius * 0.38f

        // Calculate knob position based on touchpad input
        val knobOffsetX = touchpad.knobPercentX * (baseRadius - knobRadius)
        val knobOffsetY = touchpad.knobPercentY * (baseRadius - knobRadius)
        val knobX = centerX + knobOffsetX
        val knobY = centerY + knobOffsetY

        val knobColor = if (isFireJoystick) knobColorFire else knobColorMove
        val knobHighlight = if (isFireJoystick) knobHighlightFire else knobHighlightMove
        val knobShadow = if (isFireJoystick) knobShadowFire else knobShadowMove

        // Draw base (very transparent, only as a subtle guide)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        drawGradientCircle(
            shapeRenderer,
            centerX,
            centerY,
            baseRadius,
            baseColorOuter,
            baseColorInner
        )
        shapeRenderer.end()

        // Draw base border (very subtle)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = baseBorderColor
        shapeRenderer.circle(centerX, centerY, baseRadius)
        shapeRenderer.end()

        // Draw inner guide circle (barely visible)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = guideCircleColor
        shapeRenderer.circle(centerX, centerY, baseRadius * 0.5f)
        shapeRenderer.end()

        // Draw knob shadow (offset down-left)
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        shapeRenderer.color = Color(0f, 0f, 0f, 0.3f)
        shapeRenderer.circle(knobX - 2f, knobY - 2f, knobRadius)
        shapeRenderer.end()

        // Draw knob with 3D effect
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled)
        drawKnob3D(shapeRenderer, knobX, knobY, knobRadius, knobColor, knobHighlight, knobShadow)
        shapeRenderer.end()

        // Draw knob border
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color(1f, 1f, 1f, 0.4f)
        shapeRenderer.circle(knobX, knobY, knobRadius)
        shapeRenderer.end()

        // Draw highlight arc on top of knob
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line)
        shapeRenderer.color = Color(1f, 1f, 1f, 0.5f)
        shapeRenderer.arc(knobX, knobY, knobRadius * 0.7f, 45f, 90f)
        shapeRenderer.end()
    }

    private fun drawGradientCircle(
        shapeRenderer: ShapeRenderer,
        cx: Float, cy: Float, radius: Float,
        outerColor: Color, innerColor: Color
    ) {
        val steps = 12
        for (i in steps downTo 1) {
            val t = i.toFloat() / steps
            val r = radius * t
            shapeRenderer.color = lerpColor(innerColor, outerColor, t)
            shapeRenderer.circle(cx, cy, r)
        }
    }

    private fun drawKnob3D(
        shapeRenderer: ShapeRenderer,
        cx: Float, cy: Float, radius: Float,
        baseColor: Color, highlightColor: Color, shadowColor: Color
    ) {
        // Draw main knob
        shapeRenderer.color = baseColor
        shapeRenderer.circle(cx, cy, radius)

        // Draw shadow crescent (bottom-right)
        shapeRenderer.color = shadowColor
        val shadowOffset = radius * 0.15f
        for (i in 0 until 8) {
            val angle = Math.toRadians((200 + i * 10).toDouble())
            val px = cx + cos(angle).toFloat() * (radius - 2f) + shadowOffset * 0.5f
            val py = cy + sin(angle).toFloat() * (radius - 2f) - shadowOffset * 0.5f
            shapeRenderer.circle(px, py, radius * 0.15f)
        }

        // Draw highlight crescent (top-left)
        shapeRenderer.color = highlightColor
        val highlightRadius = radius * 0.55f
        val highlightOffsetX = -radius * 0.25f
        val highlightOffsetY = radius * 0.25f
        shapeRenderer.circle(cx + highlightOffsetX, cy + highlightOffsetY, highlightRadius)

        // Restore base color for center
        shapeRenderer.color = baseColor
        shapeRenderer.circle(cx, cy, radius * 0.75f)
    }

    private fun lerpColor(a: Color, b: Color, t: Float): Color {
        return Color(
            a.r + (b.r - a.r) * t,
            a.g + (b.g - a.g) * t,
            a.b + (b.b - a.b) * t,
            a.a + (b.a - a.a) * t
        )
    }
}
