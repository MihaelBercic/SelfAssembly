package assembly

import kotlinx.serialization.Serializable

/**
 * Created by Mihael Valentin Berčič
 * on 14/07/2021 at 10:37
 * using IntelliJ IDEA
 */
@Serializable
data class Glue(val label: String, val strength: GlueStrength)
