package assembly

/**
 * Created by Mihael Valentin Berčič
 * on 14/07/2021 at 10:35
 * using IntelliJ IDEA
 */
enum class Direction(val xDiff: Int, val yDiff: Int) {
    North(0, -1),
    South(0, 1),
    West(-1, 0),
    East(1, 0);

    val opposite: Direction
        get() = when (this) {
            North -> South
            South -> North
            West -> East
            East -> West
        }
}
