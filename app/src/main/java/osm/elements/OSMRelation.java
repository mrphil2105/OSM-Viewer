package osm.elements;

import java.util.ArrayList;
import java.util.List;

public final class OSMRelation extends OSMElement {
    private final SlimOSMRelation slim;
    private final List<OSMTag> tags;

    public OSMRelation(long id) {
        this(id, new ArrayList<>(), new ArrayList<>());
    }

    public OSMRelation(long id, List<SlimOSMWay> ways, List<OSMTag> tags) {
        this.slim = new SlimOSMRelation(id, ways);
        this.tags = tags;
    }

    @Override
    public long id() {
        return slim.id();
    }

    public List<SlimOSMWay> ways() {
        return slim.ways();
    }

    @Override
    public List<OSMTag> tags() {
        return tags;
    }

    public SlimOSMRelation slim() {
        return slim;
    }
}
