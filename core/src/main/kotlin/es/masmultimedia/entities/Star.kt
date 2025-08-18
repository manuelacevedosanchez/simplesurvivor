package es.masmultimedia.entities

import com.badlogic.gdx.graphics.Color

class Star(var x: Float, var y: Float, var size: Float, var color: Color) {
    var twinkleDirection =
        if (Math.random() < 0.5) 1 else -1 // 1 para aumentar brillo, -1 para disminuir
}
