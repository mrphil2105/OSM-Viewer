package Search;

import pointsOfInterest.PointOfInterest;

import java.awt.*;

public class POIMenuItem extends MenuItem {

    private final PointOfInterest POI;

    POIMenuItem (PointOfInterest POI){
        this.POI=POI;
    }

    public PointOfInterest getPOI() {
        return POI;
    }
}
