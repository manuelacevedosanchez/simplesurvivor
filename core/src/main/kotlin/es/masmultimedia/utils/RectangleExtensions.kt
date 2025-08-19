package es.masmultimedia.utils

import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2

// Extensión para comprobar si un segmento corta el rectángulo
fun Rectangle.intersectsSegment(p1: Vector2, p2: Vector2): Boolean {
    // Lados del rectángulo
    val rectLines = listOf(
        Pair(Vector2(x, y), Vector2(x + width, y)),           // abajo
        Pair(Vector2(x, y), Vector2(x, y + height)),          // izquierda
        Pair(Vector2(x + width, y), Vector2(x + width, y + height)), // derecha
        Pair(Vector2(x, y + height), Vector2(x + width, y + height)) // arriba
    )

    for ((a, b) in rectLines) {
        if (segmentsIntersect(p1, p2, a, b)) return true
    }

    // También si empieza o termina dentro del rectángulo
    return contains(p1) || contains(p2)
}

private fun segmentsIntersect(p1: Vector2, p2: Vector2, q1: Vector2, q2: Vector2): Boolean {
    val d1 = direction(q1, q2, p1)
    val d2 = direction(q1, q2, p2)
    val d3 = direction(p1, p2, q1)
    val d4 = direction(p1, p2, q2)

    return (((d1 > 0 && d2 < 0) || (d1 < 0 && d2 > 0)) &&
        ((d3 > 0 && d4 < 0) || (d3 < 0 && d4 > 0)))
}

private fun direction(a: Vector2, b: Vector2, c: Vector2): Float {
    return (b.x - a.x) * (c.y - a.y) - (b.y - a.y) * (c.x - a.x)
}
