package osm.elements;

import geometry.Rect;

public record OSMBounds(double minlat, double minlon, double maxlat, double maxlon) {
    public Rect rect() {
        return new Rect((float) minlat(), (float) minlon(), (float) maxlat(), (float) maxlon());
    }
}
