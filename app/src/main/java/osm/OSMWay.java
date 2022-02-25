package osm;

import java.util.List;

public record OSMWay(List<OSMNode> nodes, List<OSMTag> tags) {
}
