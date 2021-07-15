import assembly.Direction
import assembly.Glue
import javafx.scene.paint.Color
import kotlinx.serialization.Serializable
import ui.asHex

/**
 * Created by Mihael Valentin Berčič
 * on 13/07/2021 at 10:59
 * using IntelliJ IDEA
 */
@Serializable
data class BlockCandidate(
    val sides: MutableMap<Direction, Glue> = mutableMapOf(),
    var color: String = Color.ORANGERED.asHex,
    var isSeed: Boolean = false
) {
    val asColor: Color by lazy { Color.web(color) }
}