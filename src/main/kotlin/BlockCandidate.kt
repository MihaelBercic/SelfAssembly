import javafx.scene.paint.Color
import kotlinx.serialization.Serializable
import ui.Direction
import ui.asHex

/**
 * Created by Mihael Valentin Berčič
 * on 13/07/2021 at 10:59
 * using IntelliJ IDEA
 */
@Serializable
data class BlockCandidate(
    val sides: MutableMap<Direction, Int> = mutableMapOf(),
    var color: String = Color.ORANGERED.asHex,
    var isSeed: Boolean = false
)