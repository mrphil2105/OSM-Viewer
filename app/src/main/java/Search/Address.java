package Search;

import osm.elements.OSMNode;

public class Address {
    private String street;
    private String housenumber;
    private String city;
    private int postcode;
    private OSMNode node;


    public int getPostcode() {
        return postcode;
    }

    public OSMNode getNode() {
        return node;
    }

    public String getCity() {
        return city;
    }

    public String getStreet() {
        return street;
    }

    public String getHousenumber() {
        return housenumber;
    }
}
