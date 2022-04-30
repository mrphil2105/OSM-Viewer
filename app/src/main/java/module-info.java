module BFST22.app.main {
    requires jdk.incubator.vector;
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.fxml;
    requires javafx.controls;
    requires earcut4j;
    requires jogamp.fat;
    requires parallelgzip;
    requires org.apache.commons.compress;

    opens view to
            javafx.fxml;
    opens dialog to
            javafx.fxml;

    exports application;
    exports canvas;
    exports collections;
    exports collections.enumflags;
    exports collections.lists;
    exports collections.observable;
    exports collections.spatial;
    exports collections.trie;
    exports dialog;
    exports drawing;
    exports features;
    exports geometry;
    exports io;
    exports navigation;
    exports osm;
    exports osm.elements;
    exports osm.tables;
    exports pointsOfInterest;
    exports Search;
    exports shaders;
    exports util;
    exports view;
}
