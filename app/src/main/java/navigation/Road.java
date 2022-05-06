package navigation;

import java.io.Serializable;

public record Road(String name, double fromLat, double fromLon, double toLat, double toLon, RoadRole role)
        implements Serializable {   
}
