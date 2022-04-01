package pointsOfInterest;

import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;

public class PointsOfInterestHBox extends HBox {

    private PointOfInterest pointOfInterest;
    private Text text;
    private Button find;
    private Button remove;

    public PointsOfInterestHBox(PointOfInterest point){
        text=new Text(point.name());
        pointOfInterest=point;
        find=new Button("Find");
        remove=new Button("Remove");
        getChildren().add(text);
        getChildren().add(find);
        getChildren().add(remove);

    }

    public PointOfInterest getPointOfInterest() {
        return pointOfInterest;
    }

    public Button getFind() {
        return find;
    }

    public Button getRemove() {
        return remove;
    }
}
