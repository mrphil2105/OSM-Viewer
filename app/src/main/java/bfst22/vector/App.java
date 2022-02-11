package bfst22.vector;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var model = new Model("data/lines8.txt");
        var view = new View(model, primaryStage);
        var view2 = new FancyView(model, new Stage());
        var controller = new Controller(model, view);
        var controller2 = new Controller(model, view2);
    }
}
