package osm.tables;

import collections.RefTable;
import osm.OSMObserver;
import osm.elements.OSMWay;

public class WayTable extends RefTable<OSMWay> implements OSMObserver {
    @Override
    public void onWay(OSMWay way) {
        put(way);
    }
}
