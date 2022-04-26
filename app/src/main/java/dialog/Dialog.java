package dialog;

import java.io.IOException;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public abstract class Dialog {
    private Stage stage;
    private boolean closable;

    protected static Dialog load(String resource) throws IOException {
        var fxmlLoader = new FXMLLoader(Dialog.class.getResource(resource));
        Parent root = fxmlLoader.load();

        var diag = fxmlLoader.<Dialog>getController();

        diag.stage = new Stage();
        diag.stage.initModality(Modality.APPLICATION_MODAL);
        diag.stage.setScene(new Scene(root));
        diag.stage.setResizable(false);
        diag.stage.setOnCloseRequest(
                e -> {
                    if (!diag.closable) e.consume();
                });

        return diag;
    }

    protected void showAndWait() {
        stage.showAndWait();
    }

    @FXML
    protected void close() {
        closable = true;
        stage.close();
    }
}
