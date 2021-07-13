package ui

import BlockCandidate
import javafx.fxml.FXML
import javafx.geometry.Insets
import javafx.scene.Cursor
import javafx.scene.control.*
import javafx.scene.layout.*
import javafx.scene.paint.Color
import javafx.stage.FileChooser
import javafx.stage.Stage
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import loadComponent


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
            canvas.cursor = Cursor.OPEN_HAND

            var lastX = 0.0
            var lastY = 0.0

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
                drawGrid()
            }

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
                    ActionType.New -> setAction(ActionType.Cancel)
                    ActionType.Cancel, ActionType.Remove -> {
                        if (currentAction == ActionType.Remove) {
                            blockSet.remove(currentCandidate)
                            repopulateFlowPane()
                        }
                        setAction(ActionType.New)
                    }
                }
                text = currentAction.name
            }
        }

        saveButton.apply {
            setOnAction {
                currentCandidate.apply {
                    if (currentAction == ActionType.Cancel) blockSet.add(this)
                    north = northField.text.toIntOrNull() ?: -1
                    south = southField.text.toIntOrNull() ?: -1
                    east = eastField.text.toIntOrNull() ?: -1
                    west = westField.text.toIntOrNull() ?: -1
                    color = colorPicker.value.asHex
                    isSeed = isSpecialCheckbox.isSelected
                }
                setAction(ActionType.New)
                currentCandidate = BlockCandidate()
                repopulateFlowPane()
            }
        }

        exportButton.setOnAction {
            FileChooser().apply {
                title = "Select export location"
                initialFileName = "Block-Candidates.json"
                selectedExtensionFilter = FileChooser.ExtensionFilter("JSON", "*.json")
                showSaveDialog(stage)?.apply {
                    createNewFile()
                    writeText(Json.encodeToString(blockSet))
                }
            }
        }
        importButton.setOnAction {
            FileChooser().apply {
                title = "Select file to import"
                selectedExtensionFilter = FileChooser.ExtensionFilter("JSON", "*.json")
                showOpenDialog(stage)?.apply {
                    val text = readText()
                    blockSet.clear()
                    blockSet.addAll(Json.decodeFromString(text))
                    repopulateFlowPane()
                    setAction(ActionType.New)
                }
            }
        }
    }

    private fun repopulateFlowPane() {
        val components = blockSet.map { blockCandidate ->
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
                inputFields.forEach { it.text = "" }
            }
            ActionType.Cancel -> {
                parentBox.disableProperty().value = false
            }
            ActionType.Remove -> parentBox.disableProperty().value = false
        }
        currentAction = actionType
        newButton.text = actionType.name
    }

    private fun drawGrid() {
        canvas.graphicsContext2D.apply {
            clearRect(0.0, 0.0, canvas.width, canvas.height)
            val blockSize = blockSize * scale
            fillRect(viewport.xOffset, viewport.yOffset, blockSize, blockSize)
        }
    }
}

val Color.asHex
    get(): String = String.format(
        "#%02x%02x%02x",
        (red * 255).toInt(),
        (green * 255).toInt(),
        (blue * 255).toInt()
    )