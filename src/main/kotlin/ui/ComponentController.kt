package ui

import BlockCandidate
import assembly.Direction
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.shape.Rectangle

/**
 * Created by Mihael Valentin Berčič
 * on 13/07/2021 at 10:33
 * using IntelliJ IDEA
 */
class ComponentController(val candidate: BlockCandidate) {

    @FXML
    lateinit var gridPane: GridPane

    @FXML
    lateinit var northLabel: Label

    @FXML
    lateinit var southLabel: Label

    @FXML
    lateinit var eastLabel: Label

    @FXML
    lateinit var westLabel: Label

    @FXML
    lateinit var northStrengthBox: Rectangle

    @FXML
    lateinit var westStrengthBox: Rectangle

    @FXML
    lateinit var eastStrengthBox: Rectangle

    @FXML
    lateinit var southStrengthBox: Rectangle

    @FXML
    fun initialize() {
        candidate.sides.forEach { (position, value) ->
            val display = when (position) {
                Direction.North -> northLabel to northStrengthBox
                Direction.South -> southLabel to southStrengthBox
                Direction.West -> westLabel to westStrengthBox
                Direction.East -> eastLabel to eastStrengthBox
            }
            display.first.text = value.label
            display.second.width = value.strength.power * 3.0
        }

        if (candidate.isSeed) gridPane.style += "-fx-border-style: dashed;" +
                "-fx-border-width: 2;" +
                "-fx-border-color: blue;"

    }
}