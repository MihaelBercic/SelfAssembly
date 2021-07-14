package ui

import javafx.beans.property.DoubleProperty
import javafx.beans.property.SimpleDoubleProperty

/**
 * Created by Mihael Valentin Berčič
 * on 13/07/2021 at 10:59
 * using IntelliJ IDEA
 */
data class ViewPort(
    var xOffset: Double = 0.0,
    var yOffset: Double = 0.0,
    val width: DoubleProperty = SimpleDoubleProperty(0.0),
    val height: DoubleProperty = SimpleDoubleProperty(0.0)
) {
    fun shouldBeDrawn(x: Double, y: Double): Boolean {
        return 0 <= x && x <= width.value + xOffset && 0 <= y && y <= height.value + yOffset
    }
}