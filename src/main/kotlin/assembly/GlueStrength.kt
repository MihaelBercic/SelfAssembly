package assembly

import kotlinx.serialization.Serializable

/**
 * Created by Mihael Valentin Berčič
 * on 14/07/2021 at 10:35
 * using IntelliJ IDEA
 */
@Serializable
enum class GlueStrength(value: Byte) {
    None(0),
    Weak(1),
    Medium(2),
    Strong(3)
}