package osm;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;

public class OSMReader {
    final XMLStreamReader reader;
    final HashMap<Long, OSMNode> nodes = new HashMap<>();
    final HashMap<Long, OSMWay> ways = new HashMap<>();
    final ArrayList<OSMRelation> relations = new ArrayList<>();
    final ArrayList<OSMNd> nds = new ArrayList<>();
    final ArrayList<OSMTag> tags = new ArrayList<>();
    final ArrayList<OSMMember> members = new ArrayList<>();
    OSMBounds bounds;
    int event;
    boolean advanceAfter;

    public OSMReader(String filename) throws IOException, XMLStreamException {
        reader = XMLInputFactory.newInstance().createXMLStreamReader(new FileInputStream(filename));
        event = reader.next();
        parseBounds();
        parseAll("node", this::parseNode);
        System.out.println("Parsed: " + nodes.size() + " nodes");
        parseAll("way", this::parseWay);
        System.out.println("Parsed: " + ways.size() + " ways");
        parseAll("relation", this::parseRelation);
        System.out.println("Parsed: " + relations.size() + " relations");
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

            runnable.run();

            if (advanceAfter) {
                if (event == XMLStreamConstants.END_DOCUMENT) return;
                advance();
            }
        }
    }

    void parseNode() {
        var id = getLong("id");
        var lon = getFloat("lon");
        var lat = getFloat("lat");
        advance();
        parseAll("tag", this::parseTag);
        nodes.put(id, new OSMNode(id, lon, lat, new ArrayList<>(tags)));
        tags.clear();
    }

    void parseWay() {
        var id = getLong("id");
        advance();
        parseAll("nd", this::parseNd);
        parseAll("tag", this::parseTag);
        ways.put(id, new OSMWay(
                nds.stream().map(nd -> nodes.get(nd.ref())).collect(Collectors.toCollection(ArrayList::new)),
                new ArrayList<>(tags)
        ));
        nds.clear();
        tags.clear();
    }

    void parseRelation() {
        advance();
        parseAll("member", this::parseMember);
        parseAll("tag", this::parseTag);
        relations.add(new OSMRelation(
                members.stream()
                        .filter(m -> m.type().equals("way"))
                        .map(m -> ways.get(m.ref()))
                        .filter(Objects::nonNull)
                        .collect(Collectors.toCollection(ArrayList::new)),
                new ArrayList<>(tags)
        ));
        members.clear();
        tags.clear();
    }

    void parseTag() {
        tags.add(new OSMTag(get("k"), get("v")));
    }

    void parseNd() {
        nds.add(new OSMNd(getLong("ref")));
    }

    void parseMember() {
        members.add(new OSMMember(get("type"), getLong("ref"), get("role")));
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
}
