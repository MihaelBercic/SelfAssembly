package ui

import javafx.fxml.FXML
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import kotlin.math.floor
import kotlin.math.min
import kotlin.random.Random


/**
 * Created by Mihael Valentin Berčič
 * on 09/07/2021 at 00:40
 * using IntelliJ IDEA
 */
class Controller {

    private val canvas = ResizeableCanvas()
    private var scale = 1.0
    private val gridSize = 10
    private val blockSize = 100.0

    private val grid = Array(gridSize) { row -> Array(gridSize) { column -> Block(row, column) } }
    private var currentDrawingFrom = grid[0][0]

    @FXML
    private var canvasPane: Pane? = null

    @FXML
    fun initialize() {
        canvasPane?.apply {
            grid.forEach { row -> row.forEach { it.color = Color.grayRgb(Random.nextInt(255)) } }

            canvas.onResize = { drawGrid() }

            children.add(canvas)
            canvas.widthProperty().bind(widthProperty())
            canvas.heightProperty().bind(heightProperty())

            setOnScroll { event ->
                scale += event.deltaY / 1000.0
                drawGrid(event.x, event.y)
            }
        }
    }


    private fun drawGrid(x: Double = 0.0, y: Double = 0.0) {
        val graphics = canvas.graphicsContext2D.apply {
            clearRect(0.0, 0.0, canvas.width, canvas.height)
        }


        val blockSize = blockSize * scale
        val horizontalCount = min(gridSize, (canvas.width / blockSize).toInt())
        val verticalCount = min(gridSize, (canvas.height / blockSize).toInt())

        val centerRow = floor(y / blockSize).toInt()
        val centerColumn = floor(x / blockSize).toInt()

        val canvasCenterX = canvas.width / 2
        val canvasCenterY = canvas.height / 2


        val differenceX = canvasCenterX - x
        val differenceY = canvasCenterY - y

        var positionY = differenceY
        var positionX = differenceX

        println("$positionY, $positionX")

        for (row in 0 until verticalCount) {
            val rectHeight = blockSize
            for (column in 0 until horizontalCount) {
                val rectWidth = blockSize
                val block = grid[row][column]
                graphics.fill = block.color
                graphics.fillRect(positionX, positionY, rectWidth, rectHeight)
                positionX += rectWidth
            }
            positionY += rectHeight
            positionX = 0.0
        }
        graphics.fill = Color.ORANGE
        graphics.fillOval(canvasCenterX, canvasCenterY, 10.0, 10.0)
    }

    data class Block(val row: Int, val column: Int, var color: Color = Color.TRANSPARENT)
}