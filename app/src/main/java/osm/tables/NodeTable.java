package osm.tables;

import collections.RefTable;
import osm.OSMObserver;
import osm.elements.OSMNode;

public class NodeTable extends RefTable<OSMNode> implements OSMObserver {
    @Override
    public void onNode(OSMNode node) {
        put(node);
    }
}
