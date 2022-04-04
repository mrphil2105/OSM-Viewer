package pointsOfInterest;

import javafx.scene.control.Button;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;

public class PointsOfInterestHBox extends HBox {

    private PointOfInterest pointOfInterest;
    private Text text;
    private Button find;
    private Button remove;

    public PointsOfInterestHBox(PointOfInterest point){
        setMaxWidth(140);
        text=new Text(point.name());
        text.setFont(new Font(15));
        pointOfInterest=point;
        find=new Button("Find");
        remove=new Button("Remove");
        var width=90;
        var fontSize =10;
        find.setPrefSize(width,10);
        remove.setPrefSize(width,10);
        find.setFont(new Font(fontSize));
        remove.setFont(new Font(fontSize));
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
