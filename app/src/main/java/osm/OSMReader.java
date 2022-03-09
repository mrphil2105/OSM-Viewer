package osm;

import collections.*;
import collections.lists.DoubleList;
import collections.lists.IntList;
import org.locationtech.jts.geom.*;
import org.locationtech.jts.operation.linemerge.LineMerger;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class OSMReader {
    enum Parseable {
        node,
        way,
        relation,
        tag,
        nd,
        member
    }

    final XMLStreamReader reader;

    // `i` is tag key and `i + 1` is tag value
    final ArrayList<String> tags = new ArrayList<>();

    // Maps from a node ref to an index for the below lists
    final RefTable nodeRefs = new RefTable();
    // `i` is lon and `i + 1` is lat
    final DoubleList nodeCoords = new DoubleList();
    // `i` is tag start index and `i + 1` is end index
    final IntList nodeTags = new IntList();

    // Maps from a way ref to an index for the below lists
    final RefTable wayRefs = new RefTable();
    // Contains lists of node indices
    final ArrayList<IntList> wayNodes = new ArrayList<>();
    // `i` is tag start index and `i + 1` is end index
    final IntList wayTags = new IntList();
    // Tag indices for way tags with drawing information
    final IntList wayDrawables = new IntList();

    // Contains lists of inner way indices
    final ArrayList<IntList> relationInnerWays = new ArrayList<>();
    // Contains lists of outer way indices
    final ArrayList<IntList> relationOuterWays = new ArrayList<>();
    // `i` is tag start index and `i + 1` is end index
    final IntList relationTags = new IntList();
    // Tag indices for relation tags with drawing information
    final IntList relationDrawables = new IntList();

    OSMBounds bounds;
    int event;
    boolean advanceAfter;

    public OSMReader(InputStream stream) throws XMLStreamException {
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        event = reader.next();

        parseBounds();

        parseAll(Parseable.node, this::parseNode);
        System.out.println("Parsed: " + nodeRefs.size() + " nodes");

        parseAll(Parseable.way, this::parseWay);
        System.out.println("Parsed: " + wayRefs.size() + " ways");

        parseAll(Parseable.relation, this::parseRelation);
        System.out.println("Parsed: " + relationOuterWays.size() + " relations");
    }

    void advance() {
        try {
            event = reader.next();
        } catch (XMLStreamException e) {
            // A method that throws cannot be coerced to a Runnable.
            // Since we want to call advance from a Runnable, we must wrap exceptions.
            throw new RuntimeException(e);
        }
    }

    void parseBounds() {
        while (event != XMLStreamConstants.START_ELEMENT || !reader.getLocalName().equals("bounds")) {
            advance();
        }

        bounds =
                new OSMBounds(
                        getDouble("minlat"), getDouble("minlon"), getDouble("maxlat"), getDouble("maxlon"));
        advance();
    }

    void parseAll(Parseable parseable, Runnable runnable) {
        while (true) {
            advanceAfter = true;
            while (event != XMLStreamConstants.START_ELEMENT) {
                if (event == XMLStreamConstants.END_DOCUMENT) return;
                advance();
            }

            if (!reader.getLocalName().equals(parseable.name())) {
                advanceAfter = false;
                return;
            }

            runnable.run();

            if (advanceAfter) {
                if (event == XMLStreamConstants.END_DOCUMENT) return;
                advance();
            }
        }
    }

    void parseNode() {
        nodeRefs.put(getLong("id"), nodeCoords.size());

        //TODO: Find equations that can translate sphere to a flat earth (eller noget i den stil)
        nodeCoords.add((getDouble("lon") - (bounds.minlon() + bounds.maxlon()) / 2) * 5600);
        nodeCoords.add((getDouble("lat") - (bounds.minlat() + bounds.maxlat()) / 2) * 10000);
        var tagStart = tags.size();

        advance();
        parseAll(Parseable.tag, this::parseNodeTag);

        var tagEnd = tags.size();
        nodeTags.add(tagStart);
        nodeTags.add(tagEnd);
    }

    void parseWay() {
        var id = getLong("id");
        var way = wayNodes.size();
        wayRefs.put(id, way);
        wayNodes.add(new IntList());
        var wayDrawablesSize = wayDrawables.size();
        var tagStart = tags.size();

        advance();
        parseAll(Parseable.nd, this::parseNd);
        parseAll(Parseable.tag, this::parseWayTag);

        var tagEnd = tags.size();
        wayTags.add(tagStart);
        wayTags.add(tagEnd);

        var diff = wayDrawables.size() - wayDrawablesSize;
        switch (diff) {
            case 0 -> wayDrawables.add(-1);
            case 1 -> {} // 1 is expected
            default -> { // Multiple drawing candidates... Choose first valid one
                var drawable = Drawable.Unknown;
                var i = tagStart;
                for (; i < tagEnd; i += 2) {
                    drawable = Drawable.fromTag(tags.get(i), tags.get(i + 1));
                    if (drawable != Drawable.Unknown) break;
                }

                if (drawable == Drawable.Unknown) i = -1;

                System.out.printf(
                        "Way with %s drawable tags (ignoring all except first): id=%d%n", diff, id);
                wayDrawables.set(way, i);
                wayDrawables.truncate(diff - 1);
            }
        }
    }

    void parseRelation() {
        var relation = relationInnerWays.size();
        relationInnerWays.add(new IntList());
        relationOuterWays.add(new IntList());
        var relationDrawablesSize = relationDrawables.size();
        var tagStart = tags.size();

        advance();
        parseAll(Parseable.member, this::parseMember);
        parseAll(Parseable.tag, this::parseRelationTag);

        var tagEnd = tags.size();
        relationTags.add(tagStart);
        relationTags.add(tagEnd);

        // TODO: Clean up dupe code
        // Can't be bothered tho, since I'll probably refactor all this later anyway.
        var diff = relationDrawables.size() - relationDrawablesSize;
        switch (diff) {
            case 0 -> relationDrawables.add(-1);
            case 1 -> {} // 1 is expected
            default -> { // Multiple drawing candidates... Choose first valid one
                var drawable = Drawable.Unknown;
                var i = tagStart;
                for (; i < tagEnd; i += 2) {
                    drawable = Drawable.fromTag(tags.get(i), tags.get(i + 1));
                    if (drawable != Drawable.Unknown) break;
                }

                if (drawable == Drawable.Unknown) i = -1;

                System.out.printf(
                        "Relation with %s drawable tags (ignoring all except first): id=%s%n", diff, get("id"));
                relationDrawables.set(relation, i);
                relationDrawables.truncate(diff - 1);
            }
        }
    }

    void parseRelationTag() {
        var k = get("k");
        var tag = Tag.from(k);
        if (tag == null) return;

        if (tag.drawable) {
            relationDrawables.add(tags.size());
        }

        tags.add(k.intern());
        tags.add(get("v").intern());
    }

    void parseNodeTag() {
        var k = get("k");
        var tag = Tag.from(k);
        if (tag == null) return;

        tags.add(k.intern());
        tags.add(get("v").intern());
    }

    void parseWayTag() {
        var k = get("k");
        var tag = Tag.from(k);
        if (tag == null) return;

        if (tag.drawable) {
            wayDrawables.add(tags.size());
        }

        tags.add(k.intern());
        tags.add(get("v").intern());
    }

    void parseNd() {
        var last = wayNodes.get(wayNodes.size() - 1);
        last.add(nodeRefs.get(getLong("ref")));
    }

    void parseMember() {
        if (get("type").equals("way")) {
            IntList last;
            switch (get("role")) {
                case "inner" -> last = relationInnerWays.get(relationInnerWays.size() - 1);
                case "outer" -> last = relationOuterWays.get(relationOuterWays.size() - 1);
                default -> {
                    return;
                }
            }
            last.add(wayRefs.get(getLong("ref")));
        }
    }

    String get(String attr) {
        return reader.getAttributeValue(null, attr);
    }

    long getLong(String attr) {
        return Long.parseLong(get(attr));
    }

    double getDouble(String attr) {
        return Double.parseDouble(get(attr));
    }

    void extendWithNodes(ArrayList<Vector2D> points, int way) {
        var nodes = wayNodes.get(way);
        var start = 0;
        var stop = nodes.size();
        var step = 1;

        var last = nodes.get(nodes.size() - 1);
        if (points.size() > 0 && points.get(points.size() - 1).equals(new Vector2D(nodeCoords.get(last), nodeCoords.get(last + 1)))) {
            start = stop;
            stop = 0;
            step = -1;
        }

        for (int j = start; j < stop; j += step) {
            var node = nodes.get(j);
            if (node == -1) continue;
            points.add(new Vector2D(nodeCoords.get(node), nodeCoords.get(node + 1)));
        }
    }

    public Polygons createPolygons() {
        var polygons = new Polygons();
        var points = new ArrayList<Vector2D>();

        var innerPoints = new ArrayList<Vector2D>();
        var holeIndices = new IntList();
        for (int i = 0; i < relationOuterWays.size(); i++) {
            var rd = relationDrawables.get(i);

            if (rd == -1) continue;

            var drawable = Drawable.fromTag(tags.get(rd), tags.get(rd + 1));
            if (drawable == Drawable.Ignored) continue;

            var lines = new ArrayList<Geometry>();
            var geometryFactory = new GeometryFactory();

            var outerWays = relationOuterWays.get(i);
            for (int j = 0; j < outerWays.size(); j++) {
                int way = outerWays.get(j);
                if (way == -1) continue;
                wayDrawables.set(way, -1); // Don't draw later in the individual way drawing step
                var nodes = wayNodes.get(way);
                var arr = Arrays.stream(nodes.toArray()).mapToObj(n -> new Coordinate(nodeCoords.get(n), nodeCoords.get(n + 1))).toArray();
                lines.add(geometryFactory.createLineString(Arrays.copyOf(arr, arr.length, Coordinate[].class)));
            }

            var innerWays = relationInnerWays.get(i);
            //for (int j = 0; j < innerWays.size(); j++) {
            //    int way = innerWays.get(j);
            //    if (way == -1) continue;
            //    holeIndices.add(innerPoints.size());
            //    extendWithNodes(innerPoints, way);
            //    if (innerPoints.size() == holeIndices.get(holeIndices.size() - 1)) holeIndices.truncate(1);
            //}

            var merger = new LineMerger();
            merger.add(lines);
            var merged = (Collection<LineString>) merger.getMergedLineStrings();
            points.addAll(merged.stream().flatMap(l -> Arrays.stream(l.getCoordinates()).map(c -> new Vector2D(c.x, c.y))).toList());

            if (points.size() > 0 && innerPoints.size() > 0) {
                var indices = Arrays.stream(holeIndices.toArray()).map(h -> h + points.size()).toArray();
                points.addAll(innerPoints);
                polygons.addPolygon(points, indices, drawable.color, drawable.layer());
            } else {
                polygons.addPolygon(points, drawable.color, drawable.layer());
            }

            points.clear();
            innerPoints.clear();
            holeIndices.truncate(holeIndices.size());
        }

        var ways = wayRefs.values();
        for (int i = 0; i < ways.size(); i++) {
            var way = ways.get(i);
            var wd = wayDrawables.get(way);

            // Undrawable
            if (wd == -1) continue;

            var drawable = Drawable.fromTag(tags.get(wd), tags.get(wd + 1));
            if (drawable == Drawable.Ignored) continue;

            extendWithNodes(points, way);

            switch (drawable.shape) {
                case Polyline -> polygons.addLines(points, drawable.size, drawable.color, drawable.layer());
                case Fill -> polygons.addPolygon(points, drawable.color, drawable.layer());
            }

            points.clear();
        }

        return polygons;
    }
}
