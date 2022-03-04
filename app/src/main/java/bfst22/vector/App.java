package bfst22.vector;

import canvas.Model;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var model = new Model("data/denmark.ser.zip");
        new View(model, primaryStage);
    }
}
