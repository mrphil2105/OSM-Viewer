package Search;

import osm.elements.OSMNode;

import java.io.Serializable;


public record Address(String street, String houseNumber, String city, int postcode, OSMNode node) implements Serializable {
    @Override
    public String toString(){
        return street + " " + houseNumber + " " + city + " " + postcode;
    }
}