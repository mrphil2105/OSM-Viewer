package canvas;

import Search.SearchTextField;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import drawing.Category;
import geometry.Point;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Side;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import pointsOfInterest.PointOfInterest;
import pointsOfInterest.PointsOfInterestHBox;
import pointsOfInterest.PointsOfInterestVBox;

public class Controller implements MouseListener {
    private Model model;
    public Menu categories;
    private Point2D lastMouse;

    private Timer queryPointTimer;

    @FXML private MapCanvas canvas;

    @FXML private SearchTextField searchTextField;

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

    @FXML private Label statusLabel;

    @FXML private PointsOfInterestVBox pointsOfInterestVBox;

    boolean pointOfInterestMode = false;
    ContextMenu addPointOfInterestText;

    public void init(Model model) {
        this.model = model;

        canvas.init(model);
        canvas.addMouseListener(this);
        searchTextField.init(model.getAddresses());
        checkBoxBuildings.setSelected(true);
        checkBoxHighways.setSelected(true);
        checkBoxWater.setSelected(true);
        radioButtonDefaultMode.setSelected(true);
        radioButtonCar.setSelected(true);
        setStyleSheets("style.css");

        statusLabel.textProperty().bind(Bindings.concat("Nearest road: ", model.nearestRoadProperty()));
        pointsOfInterestVBox.init(model.getPointsOfInterest());

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

    @FXML
    public void handleKeyTyped() {
        searchTextField.handleSearchChange();
    }

    @FXML
    public void handleSearchClick() {
        var address = searchTextField.handleSearch();
        if (address == null) return; //TODO: handle exception and show message?
        Point point = Point.geoToMap(new Point((float)address.node().lon(),(float)address.node().lat()));

        zoomOn(point);

    }

    @FXML
    public void handleInFocus(){
        searchTextField.showHistory();
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
    public void mouseClicked(MouseEvent mouseEvent) {

      if (pointOfInterestMode){
          Point point = canvas.canvasToMap(new Point((float)mouseEvent.getX(),(float)mouseEvent.getY()));
          var cm = new ContextMenu();
          var tf = new TextField("POI name");
          var mi = new CustomMenuItem(tf);
          mi.setHideOnClick(false);
          cm.getItems().add(mi);

          cm.show(canvas, Side.LEFT, mouseEvent.getX(), mouseEvent.getY());
          tf.requestFocus();
          canvas.giveFocus();

          tf.setOnAction(e -> {
              addPointOfInterest(new PointOfInterest(point.x(),point.y(),tf.getText()));
              cm.hide();
          });
        pointOfInterestMode=false;
        addPointOfInterestText.hide();

      }
    }

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
    public void mouseMoved(MouseEvent mouseEvent) {
        if (queryPointTimer != null) {
            queryPointTimer.cancel();
        }

        queryPointTimer = new Timer();
        queryPointTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> {
                    var mousePoint = new Point(mouseEvent.getX(), mouseEvent.getY());
                    var queryPoint = canvas.canvasToMap(mousePoint);
                    model.setQueryPoint(queryPoint);
                });
            }
        }, 50);

        if (pointOfInterestMode){
            addPointOfInterestText.show(canvas, Side.LEFT, mouseEvent.getX()+140, mouseEvent.getY()-30);
        }
    }

    @Override
    public void mouseDragged(MouseEvent mouseEvent) {
        canvas.pan(
                (float) (mouseEvent.getX() - lastMouse.getX()),
                (float) (mouseEvent.getY() - lastMouse.getY()));
        lastMouse = new Point2D(mouseEvent.getX(), mouseEvent.getY());
    }

    @Override
    public void mouseWheelMoved(MouseEvent mouseEvent) {
        if (mouseEvent.getRotation()[0] == 0.0) {
            canvas.zoom(
                    (float) Math.pow(1.05, mouseEvent.getRotation()[1]),
                    mouseEvent.getX(),
                    mouseEvent.getY());
        } else {
            canvas.zoom(
                    (float) Math.pow(1.15, mouseEvent.getRotation()[0]),
                    mouseEvent.getX(),
                    mouseEvent.getY());
        }
    }

    public void setStyleSheets(String stylesheet) {
        leftVBox.getStylesheets().clear();
        leftVBox.getStylesheets().add(getClass().getResource(stylesheet).toExternalForm());
        rightVBox.getStylesheets().clear();
        rightVBox.getStylesheets().add(getClass().getResource(stylesheet).toExternalForm());
    }

    public void center(Point point) {
        canvas.center(point);
    }
    public void zoomOn(Point point) {
        canvas.zoomOn(point);
    }

    public void addPointOfInterest(PointOfInterest point){
        model.getPointsOfInterest().add(point);
        pointsOfInterestVBox.update();
        for (Node n:pointsOfInterestVBox.getChildren()){

            if (((PointsOfInterestHBox)n).getPointOfInterest()==point){
               var hBox = (PointsOfInterestHBox)n;
               hBox.getFind().setOnAction(e -> {
                  zoomOn(new Point(hBox.getPointOfInterest().lon(),hBox.getPointOfInterest().lat()));
               });
               hBox.getRemove().setOnAction(e -> {
                    model.getPointsOfInterest().remove(hBox.getPointOfInterest());
                    pointsOfInterestVBox.update();
               });
            }
        }
    }

    @FXML
    public void enterPointOfInterestMode(ActionEvent actionEvent) {
        if (addPointOfInterestText==null){
            addPointOfInterestText=new ContextMenu();
            var ta = new Text("Add point of Interest");
            var mi = new CustomMenuItem(ta);
            mi.setHideOnClick(false);
            addPointOfInterestText.getItems().add(mi);
            addPointOfInterestText.requestFocus();
            canvas.giveFocus();
        }

        pointOfInterestMode=true;
    }
}
