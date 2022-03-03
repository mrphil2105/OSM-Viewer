package bfst22.vector;

import canvas.Model;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var model = new Model("C:/Users/jonas/Downloads/tbornholm.xml");
        new View(model, primaryStage);
    }
}
