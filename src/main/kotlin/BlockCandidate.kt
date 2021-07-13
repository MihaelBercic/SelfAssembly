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
    var north: Int = -1,
    var south: Int = -1,
    var east: Int = -1,
    var west: Int = -1,
    var color: String = Color.ORANGERED.asHex,
    var isSeed: Boolean = false
)