package assembly

import BlockCandidate
import javafx.scene.canvas.GraphicsContext
import ui.ViewPort
import ui.asCoordinates

/**
 * Created by Mihael Valentin Berčič
 * on 14/07/2021 at 10:34
 * using IntelliJ IDEA
 */
data class Node(
    val position: Int,
    val blockCandidate: BlockCandidate,
    val neighbours: MutableMap<Direction, Node> = mutableMapOf()
) {

    fun getOpposite(direction: Direction) = blockCandidate.sides[direction.opposite]?.let { direction to it }

    fun draw(drawn: MutableSet<Int>, viewport: ViewPort, blockSize: Double, graphics: GraphicsContext) {
        val coordinate = position.asCoordinates
        val x = coordinate.first
        val y = coordinate.second

        val xScreen = x * blockSize + viewport.xOffset
        val yScreen = y * blockSize + viewport.yOffset

        if (!drawn.contains(position) && viewport.shouldBeDrawn(xScreen + blockSize, yScreen + blockSize)) {
            drawn.add(position)
            graphics.fill = blockCandidate.asColor
            graphics.fillRect(xScreen, yScreen, blockSize, blockSize)
            neighbours.values.forEach { it.draw(drawn, viewport, blockSize, graphics) }
        }
    }

    override fun toString(): String = "Node ${position.asCoordinates}"
}