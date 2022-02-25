package bfst22.vector;

import collections.Vector2D;
import javafx.scene.paint.Color;
import osm.OSMWay;

import java.util.Arrays;
import java.util.List;

public class Drawable implements Comparable<Drawable> {
    public final List<Vector2D> points;
    Type type = null;
    public Drawable(OSMWay way) {
        points = way.nodes().stream().map(n -> new Vector2D(n.lon(), n.lat())).toList();

        for (var tag : way.tags()) {
            switch (tag.key()) {
                case "landuse" -> {
                    switch (tag.value()) {
                        case "sand" -> type = Type.Sand;
                        case "residential" -> type = Type.Residential;
                        case "tree", "forest", "grass" -> type = Type.Nature;
                        default -> type = Type.Area;
                    }
                    return;
                }
                case "area" -> {
                    type = Type.Area;
                    return;
                }
                case "building" -> {
                    type = Type.Building;
                    return;
                }
                case "highway" -> {
                    switch (tag.value()) {
                        case "motorway" -> type = Type.Motorway;
                        case "primary" -> type = Type.Primary;
                        case "secondary" -> type = Type.Secondary;
                        case "tertiary" -> type = Type.Tertiary;
                        case "road" -> type = Type.Road;
                        case "path" -> type = Type.Path;
                    }
                    return;
                }
            }
        }

        if (type == null) {
            System.out.println("Way with unknown tag: " + Arrays.toString(way.tags().toArray()));
        }
    }

    public Type getType() {
        return type;
    }

    @Override
    public int compareTo(Drawable o) {
        return Integer.compare(type.ordinal(), o.type.ordinal());
    }

    public enum Type {
        Area(Shape.Fill, Color.LIGHTGREEN, 0),
        Sand(Shape.Fill, Color.SANDYBROWN, 0),
        Residential(Shape.Fill, Color.LIGHTYELLOW, 0),
        Nature(Shape.Fill, Color.DARKGREEN, 0),
        Building(Shape.Fill, Color.ORANGE, 0),
        Path(Shape.Polyline, Color.LIGHTBLUE, 0.1f),
        Road(Shape.Polyline, Color.GREY, 0.5f),
        Tertiary(Shape.Polyline, Color.LIGHTGREY, 1.2f),
        Secondary(Shape.Polyline, Color.LIGHTPINK, 1.4f),
        Primary(Shape.Polyline, Color.MAGENTA, 1.6f),
        Motorway(Shape.Polyline, Color.DARKMAGENTA, 1.8f);

        final Shape shape;
        final Color color;
        final float size;

        Type(Shape shape, Color color, float size) {
            this.shape = shape;
            this.color = color;
            this.size = size;
        }

        enum Shape {Polyline, Fill}
    }
}
