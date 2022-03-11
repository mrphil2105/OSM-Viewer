package osm.elements;

import collections.Entity;

import java.util.List;

public final class SlimOSMWay extends Entity {
    private final long id;
    private final List<SlimOSMNode> nodes;

    public SlimOSMWay(long id, List<SlimOSMNode> nodes) {
        this.id = id;
        this.nodes = nodes;
    }

    @Override
    public long id() {
        return id;
    }

    public List<SlimOSMNode> nodes() {
        return nodes;
    }
}
