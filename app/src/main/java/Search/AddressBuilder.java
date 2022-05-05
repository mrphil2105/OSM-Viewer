package Search;

import java.io.Serializable;

public class AddressBuilder implements Serializable {
    private String street, house, floor, side, postcode, city;
    private float lat, lon;

    public String getStreet(){
        return street;
    }

    public String getCity(){
        return city;
    }

    public void street(String street) {
        this.street = street;
    }

    public void house(String house) {
        this.house = house;
    }

    public void floor(String floor) {
        this.floor = floor;
    }

    public void side(String side) {
        this.side = side;
    }

    public void postcode(String postcode) {
        this.postcode = postcode;
    }

    public void city(String city) {
        this.city = city;
    }

    public void lat(float lat){
        this.lat = lat;
    }
    public void lon(float lon){
        this.lon = lon;
    }

    public Address build() {
        return new Address(street, house, floor, side, postcode, city, lat, lon);
    }
}
