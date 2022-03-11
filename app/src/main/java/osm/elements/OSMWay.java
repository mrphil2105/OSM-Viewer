package osm.elements;

import java.util.ArrayList;
import java.util.List;

public final class OSMWay extends OSMElement {
    private final SlimOSMWay slim;
    private final List<OSMTag> tags;

    public OSMWay(long id) {
        this(id, new ArrayList<>(), new ArrayList<>());
    }

    public OSMWay(long id, List<SlimOSMNode> nodes, List<OSMTag> tags) {
        this.slim = new SlimOSMWay(id, nodes);
        this.tags = tags;
    }

    @Override
    public long id() {
        return slim.id();
    }

    public List<SlimOSMNode> nodes() {
        return slim.nodes();
    }

    @Override
    public List<OSMTag> tags() {
        return tags;
    }

    public SlimOSMWay slim() {
        return slim;
    }
}
