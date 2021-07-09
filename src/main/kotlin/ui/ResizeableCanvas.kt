package ui

/**
 * Created by Mihael Valentin Berčič
 * on 08/07/2021 at 15:12
 * using IntelliJ IDEA
 */
class ResizeableCanvas : javafx.scene.canvas.Canvas() {

    override fun isResizable(): Boolean = true

    override fun prefHeight(width: Double): Double = height
    override fun prefWidth(height: Double): Double = width

    var onResize = {
        graphicsContext2D.fillOval(10.0, 10.0, 10.0, 10.0)
        println("Drawing")
    }

    init {
        graphicsContext2D.apply {
            widthProperty().addListener { _, _, _ -> onResize() }
            heightProperty().addListener { _, _, _ -> onResize() }
        }
    }
}