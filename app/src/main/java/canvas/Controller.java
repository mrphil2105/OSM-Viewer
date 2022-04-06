package canvas;

import Search.AutofillTextField;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import drawing.Category;
import geometry.Point;
import java.util.Arrays;
import java.util.EventListener;

import javafx.beans.Observable;
import javafx.beans.property.ObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

public class Controller implements MouseListener {
    public Menu categories;
    private Point2D lastMouse;
    private ScaleBar scaleBar = new ScaleBar();
    private float currentScale;
    private float zoomLevel = 0;

    @FXML private MapCanvas canvas;

    @FXML private AutofillTextField searchTextField;

    @FXML private TextField fromRouteTextField;

    @FXML private TextField toRouteTextField;

    @FXML private Button routeButton;

    @FXML private RadioButton radioButtonCar;

    @FXML private RadioButton radioButtonBikeOrWalk;

    @FXML private CheckBox checkBoxBuildings;

    @FXML private CheckBox checkBoxHighways;

    @FXML private CheckBox checkBoxWater;

    @FXML private RadioButton radioButtonColorBlind;

    @FXML private RadioButton radioButtonDefaultMode;

    @FXML private RadioButton radioButtonPartyMode;

    @FXML private ToggleGroup groupRoute;

    @FXML private ToggleGroup groupMode;

    @FXML private VBox leftVBox;

    @FXML private VBox rightVBox;

    @FXML private Label scaleBarText;

    @FXML private Rectangle scaleBarRectangle;

    @FXML private Label zoomLevelText;

    public void init(Model model) {
        canvas.init(model);
        canvas.addMouseListener(this);
        searchTextField.init(model.getAddresses());
        checkBoxBuildings.setSelected(true);
        checkBoxHighways.setSelected(true);
        checkBoxWater.setSelected(true);
        radioButtonDefaultMode.setSelected(true);
        radioButtonCar.setSelected(true);
        setStyleSheets("style.css");
        initScaleBar(model);
        updateZoom();

        // FIXME: yuck
        categories
                .getItems()
                .addAll(
                        Arrays.stream(Category.values())
                                .map(
                                        c -> {
                                            var cb = new CheckBox(c.toString());
                                            cb.setStyle("-fx-text-fill: #222222");
                                            cb.selectedProperty().set(canvas.categories.isSet(c));

                                            canvas.categories.addObserver(
                                                    e -> {
                                                        if (e.variant() == c) {
                                                            cb.setSelected(e.enabled());
                                                        }
                                                    });

                                            cb.selectedProperty()
                                                    .addListener(
                                                            ((observable, oldValue, newValue) -> {
                                                                if (newValue) canvas.categories.set(c);
                                                                else canvas.categories.unset(c);
                                                            }));

                                            var m = new CustomMenuItem(cb);
                                            m.setHideOnClick(false);

                                            return m;
                                        })
                                .toList());
    }

    public void dispose() {
        canvas.dispose();
    }

    public void initScaleBar(Model model){
        currentScale = (float) scaleBar.getScaleBarDistance(
            model.bounds.getBottomLeft().x(), 
            model.bounds.getBottomLeft().y(), 
            model.bounds.getBottomRight().x(), 
            model.bounds.getBottomRight().y()) * canvas.getScale();
        handleScaleBar();
    }

    public void handleScaleBar(){
        var newScale = (float) (currentScale *  (1/canvas.getZoomScale()));
        if (newScale < 1000){
            scaleBarText.setText(Float.toString((float) (Math.round(newScale*100.0)/100.0)) + "m");
        } else {
            scaleBarText.setText(Float.toString((float) (Math.round((newScale/1000)*100.0)/100.0)) + "km");
        } 
    }

    @FXML void handleZoomInButton(){
        canvas.zoomChange(true);
        updateZoom();
    }

    @FXML void handleZoomOutButton(){
        canvas.zoomChange(false);
        updateZoom();
    }

    @FXML
    public void handleKeyTyped() {
        searchTextField.handleSearchChange();
    }

    @FXML
    public void handleSearchClick() {
        var address = searchTextField.handleSearch();
        if (address == null) return; //TODO: handle exception and show message?
        Point point = Point.geoToMap(new Point((float)address.node().lon(),(float)address.node().lat()));
        canvas.setZoom(25);
        canvas.center(point);
    }

    @FXML
    public void handleRouteClick() {
        fromRouteTextField.clear();
        toRouteTextField.clear();
    }

    @FXML
    public void handleDefaultMode() {
        if (radioButtonDefaultMode.isSelected()) {
            setStyleSheets("style.css");
            canvas.setShader(Renderer.Shader.DEFAULT);
        }
    }

    @FXML
    public void handleColorblind() {
        if (radioButtonColorBlind.isSelected()) {
            setStyleSheets("colorblindStyle.css");
            canvas.setShader(Renderer.Shader.MONOCHROME);
        }
    }

    @FXML
    public void handlePartyMode() {
        if (radioButtonPartyMode.isSelected()) {
            setStyleSheets("partyStyle.css");
            canvas.setShader(Renderer.Shader.PARTY);
        }
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
        float zoom;
        if (mouseEvent.getRotation()[0] == 0.0) {
            if (mouseEvent.getRotation()[1] < 0 ){}
            zoom = (float) Math.pow(1.05, mouseEvent.getRotation()[1]);
            canvas.zoom(
                    zoom,
                    mouseEvent.getX(),
                    mouseEvent.getY());
        } else {
            zoom = (float) Math.pow(1.15, mouseEvent.getRotation()[0]);
            canvas.zoom(
                    zoom,
                    mouseEvent.getX(),
                    mouseEvent.getY());
        }
        // percentage is not correct if zoomed horizontal 
        updateZoom();
        
    }

    public void updateZoom(){
        handleScaleBar();
        zoomLevel = canvas.getZoom()*100;
        zoomLevelText.setText(Float.toString((float) (Math.round(zoomLevel*100.0)/100.0)) + "%");
    }

    public void setStyleSheets(String stylesheet) {
        leftVBox.getStylesheets().clear();
        leftVBox.getStylesheets().add(getClass().getResource(stylesheet).toExternalForm());
        rightVBox.getStylesheets().clear();
        rightVBox.getStylesheets().add(getClass().getResource(stylesheet).toExternalForm());
    }

    public void centerOn(Point point) {
        canvas.center(point);
    }
}
