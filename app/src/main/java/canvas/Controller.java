package canvas;

import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;

public class Controller implements MouseListener {
    private Point2D lastMouse;

    @FXML
    private MapCanvas canvas;

    @FXML
    private TextField searchTextField;

    @FXML
    private TextField fromRouteTextField;

    @FXML
    private TextField toRouteTextfield;

    @FXML
    private Button routeButton;

    @FXML
    private RadioButton radioButtonCar;

    @FXML
    private RadioButton radioButtonBikeOrWalk;

    @FXML
    private CheckBox checkBoxBuildings;

    @FXML
    private CheckBox checkBoxHighways;

    @FXML
    private CheckBox checkBoxWater;

    @FXML
    private RadioButton radioButtonColorBlind;

    @FXML
    private RadioButton radioButtonDefaultMode;

    @FXML
    private RadioButton radioButtonPartyMode;

    @FXML
    private ToggleGroup groupRoute;

    @FXML
    private ToggleGroup groupMode;

    public void init(Model model) {
        canvas.init(model);
        canvas.addMouseListener(this);
        checkBoxBuildings.setSelected(true);
        checkBoxHighways.setSelected(true);
        checkBoxWater.setSelected(true);
        radioButtonDefaultMode.setSelected(true);
        radioButtonCar.setSelected(true);
    }

    public void dispose() {
        canvas.dispose();
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {}

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {}

    @Override
    public void mouseExited(MouseEvent mouseEvent) {}

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {}

    @Override
    public void mouseMoved(MouseEvent mouseEvent) {}

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        canvas.pan(
                (float) (mouseEvent.getX() - lastMouse.getX()),
                (float) (mouseEvent.getY() - lastMouse.getY()));
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
    }

    @Override
    public void mouseWheelMoved(MouseEvent mouseEvent) {
        canvas.zoom(
                (float) Math.pow(1.05, mouseEvent.getRotation()[1]), mouseEvent.getX(), mouseEvent.getY());
    }
}
