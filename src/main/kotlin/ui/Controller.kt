package ui

import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color


/**
 * Created by Mihael Valentin Berčič
 * on 09/07/2021 at 00:40
 * using IntelliJ IDEA
 */
class Controller {

    private val canvas = ResizeableCanvas()
    private var scale = 1.0
    private val gridSize = 100
    private val blockSize = 100.0

    private val viewport = ViewPort()

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

    private val inputFields = mutableListOf<TextField>()
    private val digitRegex = Regex("[^0-9]")
    private val blockSet = mutableListOf<BlockCandidate>()
    private var currentAction: ActionType = ActionType.New
    private var currentCandidate: BlockCandidate = BlockCandidate()

    @FXML
    fun initialize() {
        saveButton.disableProperty().bindBidirectional(parentBox.disableProperty())
        canvasPane.apply {
            canvas.onResize = { drawGrid() }
            children.add(canvas)
            canvas.widthProperty().bind(widthProperty())
            canvas.heightProperty().bind(heightProperty())

            setOnScroll { event ->
                viewport.xOffset += event.deltaX
                viewport.yOffset += event.deltaY
                drawGrid()
            }

            setOnZoom { event ->
                scale *= event.zoomFactor
                drawGrid()
            }

            inputFields.addAll(listOf(northField, southField, eastField, westField))
        }

        inputFields.forEach { field ->
            field.setOnKeyTyped { event ->
                if (event.character.contains(digitRegex)) Alert(Alert.AlertType.ERROR, "Only digits allowed.", ButtonType.OK).apply {
                    headerText = "Please use digits only."
                    showAndWait()
                    field.deletePreviousChar()
                }
            }
        }

        colorPicker.setOnAction {
            borderPane.background = Background(BackgroundFill(colorPicker.value, CornerRadii.EMPTY, Insets.EMPTY))
        }


        newButton.apply {
            setOnAction {
                when (currentAction) {
                    ActionType.New -> {
                        parentBox.disableProperty().value = false
                        currentAction = ActionType.Cancel
                    }
                    ActionType.Cancel, ActionType.Edit -> {
                        if (currentAction == ActionType.Edit) {
                            blockSet.remove(currentCandidate)
                            repopulateFlowPane()
                        }
                        parentBox.disableProperty().value = true
                        inputFields.forEach { it.text = "" }
                        currentAction = ActionType.New
                    }
                }
                text = currentAction.name
            }
        }

        saveButton.apply {
            setOnAction {
                if (currentAction == ActionType.Cancel) blockSet.add(currentCandidate)
                val north = northField.text.toIntOrNull() ?: -1
                val south = southField.text.toIntOrNull() ?: -1
                val east = eastField.text.toIntOrNull() ?: -1
                val west = westField.text.toIntOrNull() ?: -1
                parentBox.disableProperty().value = true
                repopulateFlowPane()
            }
        }
    }

    private fun repopulateFlowPane() {
        val components = blockSet.map { blockCandidate ->
            BlockComponent(blockCandidate).apply {
                setOnMouseClicked { currentCandidate = blockCandidate }
            }
        }
        flowPane.children.setAll(components)
    }

    private fun drawGrid() {
        canvas.graphicsContext2D.apply {
            clearRect(0.0, 0.0, canvas.width, canvas.height)
            val blockSize = blockSize * scale
            fillRect(viewport.xOffset, viewport.yOffset, blockSize, blockSize)
        }
    }
}

data class ViewPort(var xOffset: Double = 0.0, var yOffset: Double = 0.0, var row: Int = 0, var column: Int = 0)

data class BlockCandidate(
    var north: Int = -1,
    var south: Int = -1,
    var east: Int = -1,
    var west: Int = -1,
    var color: String = Color.ORANGERED.asHex
)

enum class ActionType {
    New,
    Cancel,
    Edit
}

val Color.asHex
    get(): String = String.format(
        "#%02x%02x%02x",
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )