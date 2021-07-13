package ui

import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Label
import javafx.scene.layout.Background
import javafx.scene.layout.BackgroundFill
import javafx.scene.layout.BorderPane
import javafx.scene.layout.CornerRadii
import javafx.scene.paint.Color

/**
 * Created by Mihael Valentin Berčič
 * on 09/07/2021 at 13:45
 * using IntelliJ IDEA
 */
class BlockComponent(public val candidate: BlockCandidate) : BorderPane() {

    val northLabel: Label = Label(if (candidate.north >= 0) "${candidate.north}" else "")
    val southLabel: Label = Label(if (candidate.south >= 0) "${candidate.south}" else "")
    val eastLabel: Label = Label(if (candidate.east >= 0) "${candidate.east}" else "")
    val westLabel: Label = Label(if (candidate.west >= 0) "${candidate.west}" else "")

    init {
        prefWidth = 50.0
        prefHeight = 50.0
        top = northLabel
        right = eastLabel
        left = westLabel
        bottom = southLabel

        listOf(northLabel, southLabel, eastLabel, westLabel).forEach {
            it.alignment = Pos.CENTER
            it.prefWidth = 50.0
        }

        padding = Insets(2.0)
        background = Background(BackgroundFill(Color.web(candidate.color), CornerRadii.EMPTY, Insets.EMPTY))
    }


}