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
        northLabel.text = if (candidate.north >= 0) "${candidate.north}" else ""
        southLabel.text = if (candidate.south >= 0) "${candidate.south}" else ""
        eastLabel.text = if (candidate.east >= 0) "${candidate.east}" else ""
        westLabel.text = if (candidate.west >= 0) "${candidate.west}" else ""

        if (candidate.isSeed) {
            println("Adding style...")
            gridPane.style += "-fx-border-style: dashed;" +
                    "-fx-border-width: 2;" +
                    "-fx-border-color: blue;"
        }
    }
}