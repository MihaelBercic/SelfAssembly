package ui

import BlockCandidate
import assembly.Direction
import assembly.Glue
import assembly.GlueStrength
import assembly.Model
import javafx.animation.AnimationTimer
import javafx.event.ActionEvent
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
import kotlin.random.Random

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
    private val grid = ConcurrentHashMap<Int, BlockCandidate>()

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

            setOnMouseDragged { event ->
                viewport.xOffset += event.x - lastX
                viewport.yOffset += event.y - lastY
                lastX = event.x
                lastY = event.y
            }

            setOnMouseReleased { canvas.cursor = Cursor.OPEN_HAND }

            setOnScroll { event ->
                scale += event.deltaY / 1000

            }

            inputFields[Direction.North] = northField
            inputFields[Direction.West] = westField
            inputFields[Direction.East] = eastField
            inputFields[Direction.South] = southField

            strengthBoxes[Direction.North] = northStrength
            strengthBoxes[Direction.East] = eastStrength
            strengthBoxes[Direction.West] = westStrength
            strengthBoxes[Direction.South] = southStrength
            strengthBoxes.values.forEach {
                it.items.setAll(*GlueStrength.values())
                it.selectionModel.select(0)
            }

        }

        colorPicker.valueProperty().addListener { _, old, new ->
            borderPane.background = Background(BackgroundFill(new, CornerRadii.EMPTY, Insets.EMPTY))
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


        object : AnimationTimer() {
            override fun handle(now: Long) {
                canvas.graphicsContext2D.apply {
                    clearRect(0.0, 0.0, canvas.width, canvas.height)
                    val size = blockSize * scale
                    val min = (-viewport.xOffset / size).toInt()
                    val max = ((-viewport.xOffset + viewport.width.value) / size).toInt()

                    val minY = (-viewport.yOffset / size).toInt()
                    val maxY = ((-viewport.yOffset + viewport.height.value) / size).toInt()

                    // println("$size = [$min ... $max ]")
                    for (x in min..max) {
                        for (y in minY..maxY) {
                            grid[x with y]?.apply {
                                fill = asColor
                                stroke = asColor
                                strokeRect(viewport.xOffset + x * size, viewport.yOffset + y * size, size, size)
                                fillRect(viewport.xOffset + x * size, viewport.yOffset + y * size, size, size)
                            }
                        }
                    }
                }

            }
        }.start()
    }

    private fun repopulateFlowPane() {
        val components = candidateSet.map { blockCandidate ->
            val controller = ComponentController(blockCandidate)
            loadComponent("/BlockComponent.fxml", controller).apply {
                style += "-fx-background-colorCode: ${blockCandidate.colorCode};"
                setOnMouseClicked {
                    currentCandidate = blockCandidate
                    setAction(ActionType.Remove)
                    northField.text = controller.northLabel.text
                    southField.text = controller.southLabel.text
                    eastField.text = controller.eastLabel.text
                    westField.text = controller.westLabel.text

                    northStrength.selectionModel.select(blockCandidate.sides[Direction.North]?.strength ?: GlueStrength.None)
                    westStrength.selectionModel.select(blockCandidate.sides[Direction.West]?.strength ?: GlueStrength.None)
                    eastStrength.selectionModel.select(blockCandidate.sides[Direction.East]?.strength ?: GlueStrength.None)
                    southStrength.selectionModel.select(blockCandidate.sides[Direction.South]?.strength ?: GlueStrength.None)
                    isSpecialCheckbox.isSelected = currentCandidate.isSeed

                    Color.web(blockCandidate.colorCode).apply {
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
            }
            ActionType.Cancel, ActionType.Remove -> parentBox.disableProperty().value = false
        }
        currentAction = actionType
        newButton.text = actionType.name
    }

    @FXML
    private fun exportData(event: ActionEvent) {
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

    @FXML
    private fun importData(event: ActionEvent) {
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

    @FXML
    private fun save(event: ActionEvent) {
        currentCandidate.apply {
            if (currentAction == ActionType.Cancel) candidateSet.add(this)
            val map = mutableMapOf(
                Direction.North to Glue(northField.text, northStrength.value),
                Direction.West to Glue(westField.text, westStrength.value),
                Direction.East to Glue(eastField.text, eastStrength.value),
                Direction.South to Glue(southField.text, southStrength.value)
            ).filter { it.value.strength != GlueStrength.None && it.value.label.isNotEmpty() }
            sides.putAll(map)
            colorCode = colorPicker.value.asHex
            isSeed = isSpecialCheckbox.isSelected
        }
        setAction(ActionType.New)
        currentCandidate = BlockCandidate()
        repopulateFlowPane()
    }

    @FXML
    private fun startSimulation(event: ActionEvent) {
        isSimulationRunning = !isSimulationRunning
        startSimulationButton.text = if (isSimulationRunning) "Stop simulation" else "Start simulation"

        if (isSimulationRunning) {
            viewport.xOffset = canvas.width / 2
            viewport.yOffset = canvas.height / 2
            val seed = candidateSet.firstOrNull { it.isSeed } ?: throw Exception("No seed in the candidates set.")
            grid.clear()
            grid[0] = seed
            grow(0, seed)
        } else println(grid.size)
    }

    private fun grow(position: Int, candidate: BlockCandidate) {
        if (!isSimulationRunning) return
        candidate.sides.keys.forEach { direction ->
            val neighbourCoordinate = position step direction
            if (!grid.containsKey(neighbourCoordinate)) {
                GlobalScope.launch {
                    delay(100)
                    findAppropriate(neighbourCoordinate, Model.ATAM)?.apply {
                        grid[neighbourCoordinate] = this
                        grow(neighbourCoordinate, this)
                    }
                }
            }
        }

    }

    private fun findAppropriate(coordinate: Int, model: Model): BlockCandidate? {
        if (grid.containsKey(coordinate)) return null

        val futureNeighbours = Direction.values().mapNotNull { grid[coordinate step it]?.getOpposite(it) }.toMap()

        when (model) {
            Model.ATAM -> {

            }
            else -> TODO("Implement other algorithms.")
        }
        return candidateSet.firstOrNull { candidate ->
            futureNeighbours.all { (direction, value) -> candidate.sides[direction] == value }
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