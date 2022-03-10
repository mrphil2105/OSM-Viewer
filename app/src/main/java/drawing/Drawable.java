package drawing;

import javafx.scene.paint.Color;
import osm.elements.OSMElement;
import osm.elements.OSMTag;

// TODO: Missing Area
public enum Drawable {
    Island(Shape.Fill, Color.web("#f2efe9"), 0),
    Beach(Shape.Fill, Color.web("#f0e1ae"), 0),
    Sand(Shape.Fill, Color.web("#f5e9c6"), 0),
    Farmland(Shape.Fill, Color.web("#eef0d5"), 0),
    Farmyard(Shape.Fill, Color.web("#f5dcba"), 0),
    Forest(Shape.Fill, Color.web("#add19e"), 0),
    Residential(Shape.Fill, Color.web("#e0dfdf"), 0),
    Park(Shape.Fill, Color.web("#c8facc"), 0),
    Construction(Shape.Fill, Color.web("#c7c7b4"), 0),
    CampSite(Shape.Fill, Color.web("#def6c0"), 0),
    BreakWater(Shape.Fill, Color.web("#aaaaaa"), 0),
    Parking(Shape.Fill, Color.web("#eeeeee"), 0),
    Allotments(Shape.Fill, Color.web("#c9e1bf"), 0),
    LeisurePark(Shape.Fill, Color.web("#dffce2"), 0),
    Grass(Shape.Fill, Color.web("#cdebb0"), 0),
    Golf(Shape.Fill, Color.web("#def6c0"), 0),
    SportsCentre(Shape.Fill, Color.web("#dffce2"), 0),
    AmenityArea(Shape.Fill, Color.web("#ffffe5"), 0),
    Heath(Shape.Fill, Color.web("#d6d99f"), 0),
    BareRock(Shape.Fill, Color.web("#dfd9d3"), 0),
    Quarry(Shape.Fill, Color.web("#c9c7c7"), 0),
    Scrub(Shape.Fill, Color.web("#c8d7ab"), 0),
    Orchard(Shape.Fill, Color.web("#aedfa3"), 0),
    Industrial(Shape.Fill, Color.web("#ebdbe8"), 0),
    PlantNursery(Shape.Fill, Color.web("#aedfa3"), 0),
    Water(Shape.Fill, Color.web("#aad3df"), 0),
    Museum(Shape.Fill, Color.web("#f2efe9"), 0),
    Building(Shape.Fill, Color.web("#d9d0c9"), 0),
    RestArea(Shape.Fill, Color.web("#efc8c8"), 0),
    Dwelling(Shape.Fill, Color.web("#f2efe9"), 0),
    Pitch(Shape.Fill, Color.web("#aae0cb"), 0),
    Retail(Shape.Fill, Color.web("#ffd0c6"), 0),
    Prison(Shape.Fill, Color.web("#bdbdbd"), 0),
    Cliff(Shape.Polyline, Color.web("#9a9b99"), 0.1),
    Hedge(Shape.Polyline, Color.web("#add19e"), 0.05),
    Wall(Shape.Polyline, Color.web("#bfbcb8"), 0.05),
    Pier(Shape.Polyline, Color.web("#ffffff"), 0.05),
    Path(Shape.Polyline, Color.web("#edb39f"), 0.1),
    Track(Shape.Polyline, Color.web("#95dcc0"), 0.2),
    ServiceRoad(Shape.Polyline, Color.web("#ffffff"), 0.2),
    Road(Shape.Polyline, Color.web("#ffffff"), 0.4),
    TreeRow(Shape.Polyline, Color.web("#aacea3"), 0.2),
    NatureReserve(Shape.Polyline, Color.web("#b5d3ae"), 0.2), // TODO: Translucent?
    Tertiary(Shape.Polyline, Color.web("#ffffff"), 0.7),
    Secondary(Shape.Polyline, Color.web("#f7fabf"), 0.8),
    Primary(Shape.Polyline, Color.web("#fcd6a4"), 0.9),
    Motorway(Shape.Polyline, Color.web("#e892a2"), 1.0),
    Unknown(Shape.Fill, Color.BLACK, 0),
    Ignored(Shape.Fill, Color.BLACK, 0);

    public static final float length = values().length;

    public final Shape shape;
    public final Color color;
    public final double size;

    Drawable(Shape shape, Color color, double size) {
        this.shape = shape;
        this.color = color;
        this.size = size;
    }

    public float layer() {
        return ordinal() / length;
    }

    private static Drawable _default(OSMTag tag) {
        System.out.println("Unknown tag " + tag.toString());
        return Unknown;
    }

    public static Drawable from(OSMElement element) {
        var drawable = Drawable.Unknown;
        for (var tag : element.tags()) {
            drawable = Drawable.from(tag);
            if (drawable != Drawable.Unknown) break;
        }
        return drawable;
    }

    public static Drawable from(OSMTag tag) {
        if (tag == null) return Unknown;
        return switch (tag.key()) {
            case Landuse -> switch (tag.value()) {
                case "allotments" -> Allotments;
                case "plant_nursery" -> PlantNursery;
                case "basin" -> Water;
                case "cemetery", "park", "village_green", "vineyard" -> Park;
                case "residential", "commercial" -> Residential;
                case "industrial" -> Industrial;
                case "recreation_ground" -> LeisurePark;
                case "construction" -> Construction;
                case "farmland", "greenhouse_horticulture" -> Farmland;
                case "farmyard" -> Farmyard;
                case "orchard" -> Orchard;
                case "quarry" -> Quarry;
                case "military", "greenfield" -> Ignored;
                case "retail" -> Retail;
                case "isolated_dwelling" -> Dwelling;
                case "grass", "meadow" -> Grass;
                case "tree", "forest", "wood", "green_field" -> Forest;
                default -> _default(tag);
            };
            case Natural -> switch (tag.value()) {
                case "wood" -> Forest;
                case "water" -> Water;
                case "cliff" -> Cliff;
                case "scrub" -> Scrub;
                case "tree_row" -> TreeRow;
                case "grassland" -> Grass;
                case "bare_rock" -> BareRock;
                case "valley" -> Ignored;
                case "wetland", "heath" -> Heath;
                case "beach" -> Beach;
                case "sand" -> Sand;
                default -> _default(tag);
            };
            case Amenity -> switch (tag.value()) {
                case "toilets", "shelter", "biergarten" -> Building;
                case "grave_yard" -> Park;
                case "prison" -> Prison;
                case "recycling" -> Industrial;
                case "parking", "bicycle_parking", "parking_space", "bus_station", "taxi" -> Parking;
                case "school", "kindergarten", "hospital" -> AmenityArea;
                default -> _default(tag);
            };
            case Leisure -> switch (tag.value()) {
                case "beach_resort" -> Sand;
                case "park" -> Park;
                case "track" -> Track;
                case "sports_centre", "stadium" -> SportsCentre;
                case "playground", "water_park" -> LeisurePark;
                case "pitch" -> Pitch;
                case "marina", "swimming_pool" -> Water;
                case "garden" -> Grass;
                case "nature_reserve" -> NatureReserve;
                case "miniature_golf", "golf_course" -> Golf;
                default -> _default(tag);
            };
            case Building -> Building;
            case Barrier -> switch (tag.value()) {
                case "yes" -> Ignored;
                case "hedge" -> Hedge;
                case "wall", "fence", "retaining_wall" -> Wall;
                default -> _default(tag);
            };
            case Tourism -> switch (tag.value()) {
                case "camp_site" -> CampSite;
                case "museum" -> Museum;
                default -> _default(tag);
            };
            case Man_made -> switch (tag.value()) {
                case "pier" -> Pier;
                case "storage_tank", "silo" -> Building;
                case "breakwater" -> BreakWater;
                case "bunker_silo" -> Ignored;
                case "wastewater_plant", "water_works" -> Industrial;
                default -> _default(tag);
            };
            case Place -> switch (tag.value()) {
                case "island" -> Island;
                case "square", "archipelago" -> Ignored;
                default -> _default(tag);
            };
            case Highway -> switch (tag.value()) {
                case "motorway" -> Motorway;
                case "primary" -> Primary;
                case "secondary" -> Secondary;
                case "tertiary" -> Tertiary;
                case "service" -> ServiceRoad;
                case "rest_area" -> RestArea;
                case "track" -> Track;
                case "proposed" -> Ignored;
                case "road", "residential", "unclassified", "raceway", "taxiway" -> Road;
                case "bridleway",
                        "construction",
                        "crossing",
                        "cycleway",
                        "footway",
                        "give_way",
                        "living_street",
                        "path",
                        "pedestrian",
                        "steps" -> Path;
                default -> _default(tag);
            };
            default -> _default(tag);
        };
    }

    public enum Shape {
        Polyline,
        Fill
    }
}
