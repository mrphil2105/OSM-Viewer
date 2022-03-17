package Search;

import osm.elements.OSMNode;

public record Address(String street, String houseNumber, String city, int postcode, OSMNode node) {}