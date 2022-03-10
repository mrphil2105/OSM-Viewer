package osm.elements;

public record OSMTag(Key key, String value) {
    public enum Key {
        Building,
        Highway,
        Natural,
        Landuse,
        Amenity,
        Leisure,
        Barrier,
        Tourism,
        Man_made,
        Place,
        Type;

        public static Key from(String key) {
            return switch (key) {
                case "building" -> Building;
                case "highway" -> Highway;
                case "natural" -> Natural;
                case "landuse" -> Landuse;
                case "amenity" -> Amenity;
                case "leisure" -> Leisure;
                case "barrier" -> Barrier;
                case "tourism" -> Tourism;
                case "man_made" -> Man_made;
                case "place" -> Place;
                case "type" -> Type;
                default -> null;
            };
        }
    }

    public static OSMTag from(String key, String value) {
        var k = Key.from(key);
        if (k == null) return null;

        return new OSMTag(k, value);
    }
}
