package bfst22.vector;

import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var model = new Model("data/small.osm");
        new View(model, primaryStage);
        //var model = new Model("data/lines336k.txt");
        // var linesmodel = new LinesModel("data/lines336k.txt");
        //var view = new View(model, primaryStage);
        // var joglview = new JOGLView(new Stage(), linesmodel);
        // var joglview2 = new JOGLView(new Stage(), linesmodel);
        //var controller = new Controller(model, view);
    }
}
