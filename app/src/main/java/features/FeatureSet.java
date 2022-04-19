package features;

import osm.OSMObserver;

import java.io.Serializable;
import java.util.HashSet;

public class FeatureSet extends HashSet<Feature> implements OSMObserver, Serializable {}
