package bfst22.vector;

import javafx.application.Application;
import javafx.stage.Stage;
import sort.QuickSort;

import java.util.Arrays;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var model = new Model("C:/Users/jonas/Downloads/transformed.ser.zip");
        new View(model, primaryStage);
    }
}
