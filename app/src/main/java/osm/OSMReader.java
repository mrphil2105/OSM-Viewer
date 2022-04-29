package osm;

import collections.lists.ByteList;
import geometry.Rect;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import osm.elements.*;
import osm.tables.NodeTable;
import osm.tables.WayTable;
import util.ThrowingRunnable;

public class OSMReader {
    enum Parseable {
        NODE('n', true),
        WAY('w', true),
        RELATION('r', true),
        TAG('t', false),
        ND('n', false),
        MEMBER('m', false);

        final byte b;
        final boolean isContainer;

        Parseable(char c, boolean isContainer) {
            b = (byte) c;
            this.isContainer = isContainer;
        }
    }

    private InputStream stream;
    private final byte[] buf = new byte[4096];
    private int cur;

    private final List<OSMObserver> observers = new ArrayList<>();

    private final NodeTable nodes = new NodeTable();
    private final WayTable ways = new WayTable();

    private OSMElement current;
    private final List<SlimOSMNode> wayNdList = new ArrayList<>();
    private boolean atTag;

    public OSMReader() {
        addObservers(nodes, ways);
    }

    public void parse(InputStream stream) throws Exception {
        this.stream = stream;

        parseBounds();

        parseAll(Parseable.NODE, this::parseNode);
        parseAll(Parseable.WAY, this::parseWay);
        parseAll(Parseable.RELATION, this::parseRelation);

        for (var observer : observers) {
            observer.onFinish();
        }
    }

    public void addObservers(OSMObserver... observers) {
        this.observers.addAll(Arrays.asList(observers));
    }

    private byte read() throws IOException {
        atTag = false;
        cur++;

        if (cur < buf.length) {
            return buf[cur];
        }

        stream.read(buf);
        cur = 0;
        return buf[cur];
    }

    private void advance(byte until) throws IOException {
        // Read a byte until the byte read is `until`
        byte b;
        do {
            b = read();
        } while (b != until);
    }

    private void advanceQuote() throws IOException {
        advance((byte) '"');
    }

    private void advanceTag() throws IOException {
        advance((byte) '<');
        read();
        atTag = true;
    }

    private void parseBounds() throws IOException {
        // Find tag whose name starts with b
        do {
            advanceTag();
        } while (buf[cur] != 'b');

        double minlat;
        double minlon;
        double maxlat;
        double maxlon;

        advance((byte) 'l');
        read();
        var latFirst = buf[cur] == 'a';
        advanceQuote();
        if (latFirst) {
            minlat = getDouble();
            advanceQuote();
            minlon = getDouble();
        } else {
            minlon = getDouble();
            advanceQuote();
            minlat = getDouble();
        }

        advance((byte) 'l');
        latFirst = read() == 'a';
        advanceQuote();
        if (latFirst) {
            maxlat = getDouble();
            advanceQuote();
            maxlon = getDouble();
        } else {
            maxlon = getDouble();
            advanceQuote();
            maxlat = getDouble();
        }

        var bounds = new Rect((float) minlat, (float) minlon, (float) maxlat, (float) maxlon);

        for (var observer : observers) {
            observer.onBounds(bounds);
        }
    }

    private void parseAll(Parseable parseable, ThrowingRunnable runnable) throws Exception {
        while (true) {
            if (!atTag) advanceTag();

            if (buf[cur] != parseable.b) {
                if (!parseable.isContainer || buf[cur] != '/') {
                    return;
                }

                advanceTag();
            } else runnable.run();
        }
    }

    private void parseNode() throws Exception {
        advanceQuote();
        var id = getLong();
        advance((byte) 'l');
        advanceQuote();
        var lat = getDouble();
        advance((byte) 'l');
        advanceQuote();
        var lon = getDouble();

        var node = new OSMNode(id, lon, lat);
        current = node;

        parseAll(Parseable.TAG, this::parseTag);

        for (var observer : observers) {
            observer.onNode(node);
        }
    }

    private void parseWay() throws Exception {
        advanceQuote();
        var id = getLong();

        var way = new OSMWay(id);
        current = way;

        parseAll(Parseable.ND, this::parseNd);
        parseAll(Parseable.TAG, this::parseTag);

        way.setNodes(wayNdList.toArray(new SlimOSMNode[0]));
        wayNdList.clear();

        for (var observer : observers) {
            observer.onWay(way);
        }
    }

    private void parseRelation() throws Exception {
        advanceQuote();
        var id = getLong();

        var relation = new OSMRelation(id);
        current = relation;

        parseAll(Parseable.MEMBER, this::parseMember);
        parseAll(Parseable.TAG, this::parseTag);

        for (var observer : observers) {
            observer.onRelation(relation);
        }
    }

    private void parseTag() throws IOException {
        advanceQuote();
        var k = getString();
        var key = OSMTag.Key.from(k);
        if (key == null) return;

        advanceQuote();
        var v = getString().intern();
        var tag = new OSMTag(key, v);

        current.tags().add(tag);
    }

    private void parseNd() throws IOException {
        advanceQuote();
        var ref = getLong();
        var node = nodes.get(ref);
        if (node == null) return;
        wayNdList.add(node);
    }

    private void parseMember() throws IOException {
        advanceQuote();
        var type = getString();

        if (type.equals("way")) {
            advanceQuote();
            var ref = getLong();
            var way = ways.get(ref);
            if (way == null) return;

            advanceQuote();
            var role = getString();

            var member = OSMMemberWay.from(way, role);
            if (member == null) return;

            ((OSMRelation) current).members().add(member);
        }
    }

    private String getString() throws IOException {
        var list = new ByteList();
        byte b;

        while ((b = read()) != '"') list.add(b);

        return new String(list.toArray(), StandardCharsets.UTF_8);
    }

    // AKA readNum
    private long getLong() throws IOException {
        long num = 0, b;
        while ((b = read()) != '"') num = num * 10 + b - '0';
        return num;
    }

    private double getDouble() throws IOException {
        long first = 0, b;
        while ((b = read()) != '.') first = first * 10 + b - '0';

        long second = 0, i = 0;
        while ((b = read()) != '"') {
            second = second * 10 + b - '0';
            i++;
        }

        return first + second / Math.pow(10, i);
    }
}
