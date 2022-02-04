package bfst22.addressparser;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.stage.Stage;

public class GUI extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        var input = new TextField();
        var output = new TextArea();
        input.setFont(new Font(30));
        output.setFont(new Font(30));
        input.setOnAction(e -> {
            var parsed = Address.parse(input.getText());
            output.setText(parsed.toString());
        });
        var pane = new BorderPane();
        pane.setTop(input);
        pane.setCenter(output);
        var scene = new Scene(pane);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
