package drawing;

import javafx.scene.paint.Color;

public enum Drawable {
    Unknown(Shape.Fill, Color.BLACK, 0),
    Area(Shape.Fill, Color.web("#f2efe9"), 0),
    Sand(Shape.Fill, Color.web("#f5e9c6"), 0),
    Park(Shape.Fill, Color.web("#c8facc"), 0),
    Grass(Shape.Fill, Color.web("#cdebb0"), 0),
    Heath(Shape.Fill, Color.web("#d6d99f"), 0),
    Forest(Shape.Fill, Color.web("#add19e"), 0),
    Farmland(Shape.Fill, Color.web("#eef0d5"), 0),
    Farmyard(Shape.Fill, Color.web("#f5dcba"), 0),
    Water(Shape.Fill, Color.web("#aad3df"), 0),
    Residential(Shape.Fill, Color.web("#e0dfdf"), 0),
    Building(Shape.Fill, Color.web("#d9d0c9"), 0),
    Path(Shape.Polyline, Color.web("#edb39f"), 0.1),
    ServiceRoad(Shape.Polyline, Color.web("#ffffff"), 0.2),
    Road(Shape.Polyline, Color.web("#ffffff"), 0.5),
    Tertiary(Shape.Polyline, Color.web("#ffffff"), 1.4),
    Secondary(Shape.Polyline, Color.web("#f7fabf"), 1.6),
    Primary(Shape.Polyline, Color.web("#fcd6a4"), 1.8),
    Motorway(Shape.Polyline, Color.web("#e892a2"), 2.0);

    public final Shape shape;
    public final Color color;
    public final double size;

    Drawable(Shape shape, Color color, double size) {
        this.shape = shape;
        this.color = color;
        this.size = size;
    }

    public static Drawable fromTag(String key, String value) {
        return switch (key) {
            case "landuse" -> switch (value) {
                case "sand", "coastline", "beach" -> Drawable.Sand;
                case "residential", "recreation_ground", "industrial_area", "commercial" -> Drawable.Residential;
                case "park", "cemetery" -> Drawable.Park;
                case "farmland" -> Drawable.Farmland;
                case "farmyard" -> Drawable.Farmyard;
                case "grass", "grassland", "meadow" -> Drawable.Grass;
                case "tree", "forest", "scrub", "wood", "wetland", "green_field" -> Drawable.Forest;
                case "water" -> Drawable.Water;
                case "heath" -> Drawable.Heath;
                case "island" -> Drawable.Area;
                default -> {
                    // System.out.printf("Way with unknown tag: k=%s v=%s%n", key, value);
                    yield Drawable.Unknown;
                }
            };
            case "area" -> Drawable.Area;
            case "building" -> Drawable.Building;
            case "highway" -> switch (value) {
                case "motorway" -> Drawable.Motorway;
                case "primary" -> Drawable.Primary;
                case "secondary" -> Drawable.Secondary;
                case "tertiary" -> Drawable.Tertiary;
                case "road" -> Drawable.Road;
                case "path" -> Drawable.Path;
                default -> {
                    // System.out.printf("Way with unknown tag: k=%s v=%s%n", key, value);
                    yield Drawable.Unknown;
                }
            };
            default -> {
                // System.out.printf("Way with unknown tag: k=%s v=%s%n", key, value);
                yield Drawable.Unknown;
            }
        };
    }

    public boolean isUnknown() {
        return this == Unknown;
    }

    public enum Shape {
        Polyline,
        Fill
    }
}
