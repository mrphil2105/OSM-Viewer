package osm;

import java.util.List;

// Use OSMWay instead of OSMMember because I don't care about other types of members for the time being
public record OSMRelation(List<OSMWay> ways, List<OSMTag> tags) {
}
