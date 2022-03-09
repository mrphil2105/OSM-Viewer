package osm;

import java.util.List;

enum Tag {
    building(true),
    highway(true),
    natural(true),
    landuse(true),
    amenity(true),
    leisure(true),
    barrier(true),
    tourism(true),
    man_made(true),
    place(true),
    type(false);

    private static final List<Tag> values = List.of(values());
    public final boolean drawable;

    Tag(boolean drawable) {
        this.drawable = drawable;
    }

    public static Tag from(String name) {
        if (values.stream().anyMatch(t -> t.name().equals(name))) {
            return valueOf(name);
        }
        return null;
    }
}
