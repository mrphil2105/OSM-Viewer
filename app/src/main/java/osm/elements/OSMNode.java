package osm.elements;

import java.util.ArrayList;
import java.util.List;

public final class OSMNode extends OSMElement {
    private final long id;
    private final double lon;
    private final double lat;
    private final List<OSMTag> tags;

    public OSMNode(long id, double lon, double lat) {
        this(id, lon, lat, new ArrayList<>());
    }

    public OSMNode(long id, double lon, double lat, List<OSMTag> tags) {
        this.id = id;
        this.lon = lon;
        this.lat = lat;
        this.tags = tags;
    }

    @Override
    public long id() {
        return id;
    }

    public double lon() {
        return lon;
    }

    public double lat() {
        return lat;
    }

    @Override
    public List<OSMTag> tags() {
        return tags;
    }
}
