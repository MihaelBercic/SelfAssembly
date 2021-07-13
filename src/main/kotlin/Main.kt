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
        val mainController = Controller(primaryStage)
        loadComponent("/ui.fxml", mainController).apply {
            primaryStage.scene = Scene(this)
            primaryStage.show()
        }
    }
}

fun loadComponent(path: String, controller: Any? = null): Parent {
    FXMLLoader(Application::class.java.getResource(path)).apply {
        setController(controller)
        return load()
    }
}

fun main() = Application.launch(Window::class.java)