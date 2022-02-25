package osm;

import java.util.List;

public record OSMNode(long id, float lon, float lat, List<OSMTag> tags) {
}
