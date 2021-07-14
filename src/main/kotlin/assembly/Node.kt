package assembly

import BlockCandidate
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import ui.ViewPort
import ui.asCoordinates
import java.util.concurrent.ConcurrentHashMap

/**
 * Created by Mihael Valentin Berčič
 * on 14/07/2021 at 10:34
 * using IntelliJ IDEA
 */
data class Node(
    val position: Int,
    val blockCandidate: BlockCandidate,
    val neighbours: MutableMap<Direction, Node> = ConcurrentHashMap()
) {

    fun draw(drawn: MutableSet<Int>, viewport: ViewPort, blockSize: Double, graphics: GraphicsContext) {
        if (drawn.contains(position)) return
        drawn.add(position)
        val coordinate = position.asCoordinates
        val x = coordinate.first
        val y = coordinate.second

        val xScreen = x * blockSize + viewport.xOffset
        val yScreen = y * blockSize + viewport.yOffset

        if (viewport.shouldBeDrawn(xScreen + blockSize, yScreen + blockSize)) {
            graphics.fill = Color.web(blockCandidate.color)
            graphics.fillRect(viewport.xOffset + x * blockSize, viewport.yOffset + y * blockSize, blockSize, blockSize)
            neighbours.values.forEach { it.draw(drawn, viewport, blockSize, graphics) }
        }
    }

    override fun toString(): String = "Node ${position.asCoordinates}"
}