package osm.tables;

import collections.RefTable;
import osm.OSMObserver;
import osm.elements.OSMRelation;

public class RelationTable extends RefTable<OSMRelation> implements OSMObserver {
    @Override
    public void onRelation(OSMRelation relation) {
        put(relation);
    }
}
