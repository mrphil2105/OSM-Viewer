package osm;

import java.util.List;

public record OSMNode(long id, float lat, float lon, List<OSMTag> tags) {}
