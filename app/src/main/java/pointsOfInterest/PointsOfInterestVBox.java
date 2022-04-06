package pointsOfInterest;

import java.util.List;
import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class PointsOfInterestVBox extends VBox {

    private List<PointOfInterest> pointsOfInterest;

    public void init(List<PointOfInterest> points) {
        pointsOfInterest = points;
    }

    public void update() {
        for (PointOfInterest point : pointsOfInterest) {
            if (!contains(point)) {
                addPointOfInterest(point);
            }
        }
        for (Node n : getChildren()) {
            if (!pointsOfInterest.contains(((PointsOfInterestHBox) n).getPointOfInterest())) {
                removePointOfInterest(((PointsOfInterestHBox) n).getPointOfInterest());
            }
        }
    }

    public void addPointOfInterest(PointOfInterest point) {
        getChildren().add(new PointsOfInterestHBox(point));
    }

    public void removePointOfInterest(PointOfInterest point) {
        getChildren().removeIf(n -> ((PointsOfInterestHBox) n).getPointOfInterest() == point);
    }

    public boolean contains(PointOfInterest point) {
        for (Node n : getChildren()) {
            if (((PointsOfInterestHBox) n).getPointOfInterest() == point) {
                return true;
            }
        }
        return false;
    }
}
