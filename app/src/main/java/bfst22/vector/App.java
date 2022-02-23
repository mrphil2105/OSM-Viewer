package bfst22.vector;

import javafx.application.Application;
import javafx.stage.Stage;
import osm.OSMReader;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var model = new LinesModel("data/lines336k.txt");
        new View(model, primaryStage);
    }
}
