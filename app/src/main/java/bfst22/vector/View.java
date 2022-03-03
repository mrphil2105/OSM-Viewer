package bfst22.vector;

import java.io.IOException;

import canvas.Controller;
import canvas.Model;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class View {
    public View(Model model, Stage stage) throws IOException {
        var loader = new FXMLLoader(View.class.getResource("View.fxml"));

        stage.setScene(loader.load());
        Controller controller = loader.getController();
        controller.init(model);

        stage.setOnCloseRequest(event -> controller.dispose());
        stage.setTitle("OSM Viewer (OpenGL)");
        stage.show();
    }
}
