package ui

import BlockCandidate
import assembly.Direction
import assembly.Glue
import assembly.GlueStrength
import assembly.Node
import javafx.animation.AnimationTimer
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import loadComponent
import java.util.concurrent.ConcurrentHashMap


/**
 * Created by Mihael Valentin Berčič
 * on 09/07/2021 at 00:40
 * using IntelliJ IDEA
 */
class Controller(private val stage: Stage) {

    private val canvas = ResizeableCanvas()
    private var scale = 1.0
    private val gridSize = 100
    private val blockSize = 100.0

    private val viewport = ViewPort()
    private val grid = ConcurrentHashMap<Int, Node>()

    private var root: Node? = null

    @FXML
    private lateinit var canvasPane: Pane

    @FXML
    private lateinit var northField: TextField

    @FXML
    private lateinit var westField: TextField

    @FXML
    private lateinit var eastField: TextField

    @FXML
    private lateinit var southField: TextField

    @FXML
    private lateinit var colorPicker: ColorPicker

    @FXML
    private lateinit var isSpecialCheckbox: CheckBox

    @FXML
    private lateinit var newButton: Button

    @FXML
    private lateinit var importButton: Button

    @FXML
    private lateinit var saveButton: Button

    @FXML
    private lateinit var exportButton: Button

    @FXML
    private lateinit var borderPane: BorderPane

    @FXML
    private lateinit var flowPane: FlowPane

    @FXML
    private lateinit var parentBox: HBox

    @FXML
    private lateinit var startSimulationButton: Button

    @FXML
    private lateinit var northStrength: ComboBox<GlueStrength>

    @FXML
    private lateinit var eastStrength: ComboBox<GlueStrength>

    @FXML
    private lateinit var westStrength: ComboBox<GlueStrength>

    @FXML
    private lateinit var southStrength: ComboBox<GlueStrength>

    private var isSimulationRunning = false
    private val inputFields = mutableMapOf<Direction, TextField>()
    private val strengthBoxes = mutableMapOf<Direction, ComboBox<GlueStrength>>()
    private val candidateSet = mutableListOf<BlockCandidate>()
    private val drawnAlready = mutableSetOf<Int>()

    private var currentAction: ActionType = ActionType.New
    private var currentCandidate: BlockCandidate = BlockCandidate()

    private var lastX = 0.0
    private var lastY = 0.0

    @FXML
    fun initialize() {
        saveButton.disableProperty().bindBidirectional(parentBox.disableProperty())
        canvasPane.apply {
            children.add(canvas)
            canvas.widthProperty().bind(widthProperty())
            canvas.heightProperty().bind(heightProperty())
            viewport.width.bind(canvas.widthProperty())
            viewport.height.bind(canvas.heightProperty())
            canvas.cursor = Cursor.OPEN_HAND


            setOnMousePressed {
                lastX = it.x
                lastY = it.y
                canvas.cursor = Cursor.CLOSED_HAND
            }

            setOnMouseReleased {
                canvas.cursor = Cursor.OPEN_HAND
            }

            setOnMouseDragged { event ->
                viewport.xOffset += event.x - lastX
                viewport.yOffset += event.y - lastY
                lastX = event.x
                lastY = event.y
            }

            setOnScroll { event ->
                viewport.xOffset += event.deltaX
                viewport.yOffset += event.deltaY
            }

            setOnZoom { event ->
                scale *= event.zoomFactor
            }

            inputFields[Direction.North] = northField
            inputFields[Direction.West] = westField
            inputFields[Direction.East] = eastField
            inputFields[Direction.South] = southField

            strengthBoxes[Direction.North] = northStrength
            strengthBoxes[Direction.East] = eastStrength
            strengthBoxes[Direction.West] = westStrength
            strengthBoxes[Direction.South] = southStrength
            strengthBoxes.values.forEach { it.items.setAll(*GlueStrength.values()) }

        }

        colorPicker.setOnAction {
            borderPane.background = Background(BackgroundFill(colorPicker.value, CornerRadii.EMPTY, Insets.EMPTY))
        }

        newButton.setOnAction {
            when (currentAction) {
                ActionType.New -> setAction(ActionType.Cancel)
                ActionType.Cancel, ActionType.Remove -> {
                    if (currentAction == ActionType.Remove) {
                        candidateSet.remove(currentCandidate)
                        repopulateFlowPane()
                    }
                    setAction(ActionType.New)
                }
            }
        }

        saveButton.setOnAction {
            currentCandidate.apply {
                if (currentAction == ActionType.Cancel) candidateSet.add(this)
                val map = mutableMapOf<Direction, Glue>(

                )

                val northLabel = northField.text
                val northStrength = northStrength.value
                if (northLabel.isNotEmpty()) map[Direction.North] = Glue(northLabel, northStrength)

                color = colorPicker.value.asHex
                isSeed = isSpecialCheckbox.isSelected
            }
            setAction(ActionType.New)
            currentCandidate = BlockCandidate()
            repopulateFlowPane()
        }

        exportButton.setOnAction {
            FileChooser().apply {
                title = "Select export location"
                initialFileName = "Block-Candidates.json"
                selectedExtensionFilter = FileChooser.ExtensionFilter("JSON", "*.json")
                showSaveDialog(stage)?.apply {
                    createNewFile()
                    writeText(Json.encodeToString(candidateSet))
                }
            }
        }
        importButton.setOnAction {
            FileChooser().apply {
                title = "Select file to import"
                selectedExtensionFilter = FileChooser.ExtensionFilter("JSON", "*.json")
                showOpenDialog(stage)?.apply {
                    candidateSet.clear()
                    candidateSet.addAll(Json.decodeFromString(readText()))
                    repopulateFlowPane()
                    setAction(ActionType.New)
                }
            }
        }

        startSimulationButton.setOnAction {
            isSimulationRunning = !isSimulationRunning
            startSimulationButton.text = if (isSimulationRunning) "Stop simulation" else "Start simulation"

            if (isSimulationRunning) {
                viewport.xOffset = canvas.width / 2
                viewport.yOffset = canvas.height / 2
                startSimulation()
            }
        }

        object : AnimationTimer() {
            override fun handle(now: Long) {
                drawnAlready.clear()
                val graphics = canvas.graphicsContext2D
                val blockSize = blockSize * scale
                val centerX = ((-viewport.xOffset / blockSize) + (viewport.width.value / blockSize / 2)).toInt()
                val centerY = ((-viewport.yOffset / blockSize) + (viewport.height.value / blockSize / 2)).toInt()

                val position = centerX with centerY
                // println("$centerX, $centerY ... ${grid[position]}")
                graphics.clearRect(0.0, 0.0, canvas.width, canvas.height)
                grid[position]?.draw(drawnAlready, viewport, blockSize, graphics)
            }
        }.start()
    }

    private fun repopulateFlowPane() {
        val components = candidateSet.map { blockCandidate ->
            val controller = ComponentController(blockCandidate)
            loadComponent("/BlockComponent.fxml", controller).apply {
                style += "-fx-background-color: ${blockCandidate.color};"
                setOnMouseClicked {
                    currentCandidate = blockCandidate
                    setAction(ActionType.Remove)
                    northField.text = controller.northLabel.text
                    southField.text = controller.southLabel.text
                    eastField.text = controller.eastLabel.text
                    westField.text = controller.westLabel.text
                    isSpecialCheckbox.isSelected = currentCandidate.isSeed

                    Color.web(blockCandidate.color).apply {
                        colorPicker.value = this
                        borderPane.background = Background(BackgroundFill(this, CornerRadii.EMPTY, Insets.EMPTY))
                    }
                    setAction(ActionType.Remove)
                }
            }
        }
        flowPane.children.setAll(components)
    }

    private fun setAction(actionType: ActionType) {
        when (actionType) {
            ActionType.New -> {
                parentBox.disableProperty().value = true
                isSpecialCheckbox.isSelected = false
                inputFields.values.forEach { it.text = "" }
                strengthBoxes.values.forEach { it.selectionModel.clearSelection() }
            }
            ActionType.Cancel -> {
                parentBox.disableProperty().value = false
            }
            ActionType.Remove -> parentBox.disableProperty().value = false
        }
        currentAction = actionType
        newButton.text = actionType.name
    }

    private fun startSimulation() {
        val seed = candidateSet.firstOrNull { it.isSeed } ?: throw Exception("No seed in the candidates set.")
        grid.clear()
        Node(0, seed).apply {
            grid[position] = this
            grow(this)
        }

    }

    private fun grow(node: Node) {
        if (!isSimulationRunning) return
        val position = node.position
        val toGrow = mutableListOf<Node>()

        node.blockCandidate.sides.forEach { (direction, _) ->
            val coordinate = position step direction
            val directionNode = grid[coordinate]
            if (directionNode == null) {
                val candidate = findAppropriate(coordinate) ?: throw Exception("No suitable candidate found.")
                Node(coordinate, candidate).apply {
                    Direction.values().forEach { direction ->
                        val neighbour = grid[coordinate step direction]
                        if (neighbour != null) {
                            neighbours[direction] = neighbour
                            neighbour.neighbours[direction.opposite] = this
                        }
                    }
                    grid[coordinate] = this
                    node.neighbours[direction] = this
                    toGrow.add(this)
                }
            }
        }
        toGrow.forEach {
            GlobalScope.launch {
                delay(100)
                grow(it)
            }
        }
    }

    private fun findAppropriate(coordinate: Int): BlockCandidate? {
        val neededSides = Direction.values().mapNotNull { direction ->
            val neighbourCoordinate = coordinate step direction
            grid[neighbourCoordinate]?.blockCandidate?.sides?.get(direction.opposite)?.let { direction to it }
        }.toMap()
        return candidateSet.lastOrNull {
            val sides = it.sides
            neededSides.all { (direction, value) -> sides[direction] == value }
        }

    }
}

private infix fun Int.step(neighbour: Direction) = asCoordinates.let { (x, y) ->
    (x + neighbour.xDiff) with (y + neighbour.yDiff)
}

val Int.asCoordinates get() = shr(16).toShort() to and(0xFFFF).toShort()
private infix fun Number.with(y: Number) = (toInt() shl 16) or (y.toInt() and 0xFFFF)


val Color.asHex
    get(): String = String.format(
        "#%02x%02x%02x",
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )