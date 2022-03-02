package osm;

import collections.*;
import drawing.Drawable;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.InputStream;
import java.util.ArrayList;

public class OSMReader {
    final XMLStreamReader reader;

    // `i` is tag key and `i + 1` is tag value
    final ArrayList<String> tags = new ArrayList<>();

    // Maps from a node ref to an index for the below lists
    final RefTable nodeRefs = new RefTable();
    // `i` is lon and `i + 1` is lat
    final FloatList nodeCoords = new FloatList();
    // `i` is tag index and `i + 1` is count
    final IntList nodeTags = new IntList();

    // Maps from a way ref to an index for the below lists
    final RefTable wayRefs = new RefTable();
    // Contains lists of node indices
    final ArrayList<IntList> wayNodes = new ArrayList<>();
    // `i` is tag index and `i + 1` is count
    final IntList wayTags = new IntList();
    // Tag indices for way tags with way-drawable
    final IntList wayDrawables = new IntList();

    // Contains lists of way indices
    final ArrayList<IntList> relationWays = new ArrayList<>();
    // `i` is tag index and `i + 1` is count
    final IntList relationTags = new IntList();

    OSMBounds bounds;
    int event;
    boolean advanceAfter;

    public OSMReader(InputStream stream) throws XMLStreamException {
        reader = XMLInputFactory.newInstance().createXMLStreamReader(stream);
        event = reader.next();

        parseBounds();

        parseAll("node", this::parseNode);
        System.out.println("Parsed: " + nodeRefs.size() + " nodes");

        parseAll("way", this::parseWay);
        System.out.println("Parsed: " + wayRefs.size() + " ways");

        parseAll("relation", this::parseRelation);
        System.out.println("Parsed: " + relationWays.size() + " relations");

        // Sort in draw order, with undrawable ways last
        System.out.println("Sorting ways");
        wayRefs.sortByValues((i, j) -> {
            var wi = wayDrawables.get(i);
            var wj = wayDrawables.get(j);

            if (wi == -1 && wj == -1)
                return 0;
            else if (wi == -1)
                return 1;
            else if (wj == -1)
                return -1;
            else {
                var di = Drawable.fromTag(tags.get(wi), tags.get(wi + 1));
                var dj = Drawable.fromTag(tags.get(wj), tags.get(wj + 1));
                return di.compareTo(dj);
            }
        });
        System.out.println("Done sorting ways");
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

        bounds = new OSMBounds(getFloat("minlat"), getFloat("minlon"), getFloat("maxlat"), getFloat("maxlon"));
        advance();
    }

    void parseAll(String name, Runnable runnable) {
        while (true) {
            advanceAfter = true;
            while (event != XMLStreamConstants.START_ELEMENT) {
                if (event == XMLStreamConstants.END_DOCUMENT) return;
                advance();
            }

            if (!reader.getLocalName().equals(name)) {
                advanceAfter = false;
                return;
            }

            if (wayNodes.size() % 100000 == 0 && name.equals("way")) {
                System.out.println(wayNodes.size() + " ways so far");
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
        nodeCoords.add((getFloat("lon") - bounds.minlon()) * 5600);
        nodeCoords.add((getFloat("lat") - bounds.minlat()) * 10000);
        var tagStart = tags.size();

        advance();
        parseAll("tag", this::parseTag);

        var tagCount = tags.size() - tagStart;
        nodeTags.add(tagStart);
        nodeTags.add(tagCount);
    }

    void parseWay() {
        var id = getLong("id");
        wayRefs.put(id, wayNodes.size());
        wayNodes.add(new IntList());
        var wayDrawablesSize = wayDrawables.size();
        var tagStart = tags.size();

        advance();
        parseAll("nd", this::parseNd);
        parseAll("tag", this::parseTag);

        var tagCount = (tags.size() - tagStart) / 2;
        wayTags.add(tagStart);
        wayTags.add(tagCount);

        var diff = wayDrawables.size() - wayDrawablesSize;
        switch (diff) {
            case 0 -> wayDrawables.add(-1);
            case 1 -> {} // 1 is expected
            default -> {
                System.out.printf("Way with %s way-drawable tags (ignoring all except first): id=%d%n", diff, id);
                wayDrawables.truncate(diff - 1);
            }
        }
    }

    void parseRelation() {
        relationWays.add(new IntList());
        var tagStart = tags.size();

        advance();
        parseAll("member", this::parseMember);
        parseAll("tag", this::parseTag);

        var tagCount = tags.size() - tagStart;
        relationTags.add(tagStart);
        relationTags.add(tagCount);
    }

    void parseTag() {
        var k = get("k");
        var split = k.split(":", 2);
        if (wayNodes.size() > 0 && split[0].equals("way-drawable")) {
            wayDrawables.add(tags.size());
            k = split[1];
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
            var last = relationWays.get(relationWays.size() - 1);
            last.add(wayRefs.get(getLong("ref")));
        }
    }

    String get(String attr) {
        return reader.getAttributeValue(null, attr);
    }

    long getLong(String attr) {
        return Long.parseLong(get(attr));
    }

    float getFloat(String attr) {
        return Float.parseFloat(get(attr));
    }

    public Polygons createPolygons() {
        var polygons = new Polygons();

        var points = new ArrayList<Vector2D>();
        var values = wayRefs.values();
        for (int i = 0; i < values.size(); i++) {
            var way = values.get(i);
            var wd = wayDrawables.get(way);

            // All undrawable ways appear last, meaning we're done when we see the first one
            if (wd == -1)
                break;

            var drawable = Drawable.fromTag(tags.get(wd), tags.get(wd + 1));
            if (drawable.isUnknown())
                continue;

            var nodes = wayNodes.get(way);
            for (int j = 0; j < nodes.size(); j++) {
                var node = nodes.get(j);
                if (node == -1)
                    continue;
                points.add(new Vector2D(nodeCoords.get(node), nodeCoords.get(node + 1)));
            }

            switch (drawable.shape) {
                case Polyline -> polygons.addLines(points, drawable.size, drawable.color);
                case Fill -> polygons.addPolygon(points, drawable.color);
            }

            points.clear();
        }

        return polygons;
    }
}
