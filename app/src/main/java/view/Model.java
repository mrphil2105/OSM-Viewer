package view;

import Search.AddressDatabase;
import geometry.Point;
import geometry.Rect;
import io.FileParser;
import java.util.ArrayList;
import java.util.List;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import navigation.NearestNeighbor;
import pointsOfInterest.PointOfInterest;

public class Model {
    public final Rect bounds;
    private final AddressDatabase addresses;
    private final List<PointOfInterest> pointsOfInterest;
    private final NearestNeighbor nearestNeighbor;
    private final StringProperty nearestRoad = new SimpleStringProperty("none");

    public final canvas.Model canvasModel;

    public Model(String filename) throws Exception {
        try (var result = FileParser.readFile(filename)) {
            bounds = result.bounds().read().getRect();
            canvasModel = new canvas.Model(result.polygons());
            nearestNeighbor = result.nearestNeighbor().read();
            addresses = result.addresses().read();
            addresses.buildTries();
        }
        pointsOfInterest = new ArrayList<>();
    }

    public StringProperty nearestRoadProperty() {
        return nearestRoad;
    }

    public String getNearestRoad() {
        return nearestRoad.get();
    }

    public void setQueryPoint(Point query) {
        var road = nearestNeighbor.nearestTo(query);
        nearestRoadProperty().set(road);
    }

    public AddressDatabase getAddresses() {
        return addresses;
    }

    public List<PointOfInterest> getPointsOfInterest() {
        return pointsOfInterest;
    }
}
