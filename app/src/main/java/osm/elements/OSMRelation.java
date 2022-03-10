package osm.elements;

import java.util.ArrayList;
import java.util.List;

public final class OSMRelation extends OSMElement {
    private final long id;
    private final List<OSMMemberWay> members;
    private final List<OSMTag> tags;

    public OSMRelation(long id) {
        this(id, new ArrayList<>(), new ArrayList<>());
    }

    public OSMRelation(long id, List<OSMMemberWay> members, List<OSMTag> tags) {
        this.id = id;
        this.members = members;
        this.tags = tags;
    }

    @Override
    public long id() {
        return id;
    }

    public List<OSMMemberWay> members() {
        return members;
    }

    @Override
    public List<OSMTag> tags() {
        return tags;
    }
}
