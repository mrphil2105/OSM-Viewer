package Search;

import osm.elements.OSMNode;
import osm.elements.SlimOSMNode;

import java.io.Serializable;


public record Address(String street, String houseNumber, String city, int postcode, SlimOSMNode node) implements Serializable {
    @Override
    public String toString(){
        return street + " " + houseNumber + " " + city + " " + postcode;
    }
}