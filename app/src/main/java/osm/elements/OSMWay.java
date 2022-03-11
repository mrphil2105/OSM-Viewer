package osm.elements;

import java.util.ArrayList;
import java.util.List;

public final class OSMWay extends OSMElement {
    private final long id;
    private final List<OSMNode> nodes;
    private final List<OSMTag> tags;

    public OSMWay(long id) {
        this(id, new ArrayList<>(), new ArrayList<>());
    }

    public OSMWay(long id, List<OSMNode> nodes, List<OSMTag> tags) {
        this.id = id;
        this.nodes = nodes;
        this.tags = tags;
    }

    @Override
    public long id() {
        return id;
    }

    public List<OSMNode> nodes() {
        return nodes;
    }

    @Override
    public List<OSMTag> tags() {
        return tags;
    }
}
