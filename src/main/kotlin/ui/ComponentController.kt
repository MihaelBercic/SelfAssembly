package ui

import BlockCandidate
import javafx.fxml.FXML
import javafx.scene.control.Label
import javafx.scene.layout.GridPane

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
    fun initialize() {
        candidate.sides.forEach { (position, value) ->
            val label = when (position) {
                Direction.North -> northLabel
                Direction.South -> southLabel
                Direction.West -> westLabel
                Direction.East -> eastLabel
            }
            label.text = "$value"
        }

        if (candidate.isSeed) gridPane.style += "-fx-border-style: dashed;" +
                "-fx-border-width: 2;" +
                "-fx-border-color: blue;"

    }
}