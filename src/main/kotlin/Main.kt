import javafx.application.Application
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import ui.Controller


/**
 * Created by Mihael Valentin Berčič
 * on 08/07/2021 at 14:57
 * using IntelliJ IDEA
 */
class Window : Application() {

    override fun start(primaryStage: Stage) {
        val controller = Controller()
        val loader = FXMLLoader(javaClass.getResource("/ui.fxml")).apply {
            setController(controller)
        }
        val root = loader.load<Parent>()
        primaryStage.scene = Scene(root)
        primaryStage.show()
    }

}

fun main() = Application.launch(Window::class.java)