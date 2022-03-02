package drawing;

import javafx.scene.paint.Color;

public enum Drawable {
    Unknown(Shape.Fill, Color.BLACK, 0),
    Area(Shape.Fill, Color.LIGHTGREEN, 0),
    Sand(Shape.Fill, Color.SANDYBROWN, 0),
    Nature(Shape.Fill, Color.DARKGREEN, 0),
    Water(Shape.Fill, Color.BLUE, 0),
    Residential(Shape.Fill, Color.LIGHTYELLOW, 0),
    Building(Shape.Fill, Color.ORANGE, 0),
    Path(Shape.Polyline, Color.LIGHTBLUE, 0.1f),
    Road(Shape.Polyline, Color.GREY, 0.5f),
    Tertiary(Shape.Polyline, Color.LIGHTGREY, 1.2f),
    Secondary(Shape.Polyline, Color.LIGHTPINK, 1.4f),
    Primary(Shape.Polyline, Color.MAGENTA, 1.6f),
    Motorway(Shape.Polyline, Color.DARKMAGENTA, 1.8f);

    public final Shape shape;
    public final Color color;
    public final float size;

    Drawable(Shape shape, Color color, float size) {
        this.shape = shape;
        this.color = color;
        this.size = size;
    }

    public static Drawable fromTag(String key, String value) {
        return switch (key) {
            case "landuse" -> switch (value) {
                case "sand", "coastline", "beach" -> Drawable.Sand;
                case "residential", "recreation_ground", "industrial", "commercial", "military" -> Drawable.Residential;
                case "tree", "forest", "grass", "meadow", "farmyard", "farmland", "scrub", "wood", "wetland", "green_field", "cemetery" -> Drawable.Nature;
                case "water" -> Drawable.Water;
                default -> {
                    //System.out.printf("Way with unknown tag: k=%s v=%s%n", key, value);
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
                    //System.out.printf("Way with unknown tag: k=%s v=%s%n", key, value);
                    yield Drawable.Unknown;
                }
            };
            default -> {
                //System.out.printf("Way with unknown tag: k=%s v=%s%n", key, value);
                yield Drawable.Unknown;
            }
        };
    }

    public boolean isUnknown() {
        return this == Unknown;
    }

    public enum Shape {Polyline, Fill}
}
