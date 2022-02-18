package bfst22.vector;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var model = new Model("data/small.osm");
        new View(model, primaryStage);
    }
}
