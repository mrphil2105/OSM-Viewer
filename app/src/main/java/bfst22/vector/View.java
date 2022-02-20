package bfst22.vector;

import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.io.IOException;

public class View {
    public View(LinesModel model, Stage stage) throws IOException {
        var loader = new FXMLLoader(View.class.getResource("View.fxml"));

        stage.setScene(loader.load());
        Controller controller = loader.getController();
        controller.init(model);

        stage.setOnCloseRequest(event -> controller.dispose());
        stage.setTitle("OSM Viewer (OpenGL)");
        stage.show();
    }
}
