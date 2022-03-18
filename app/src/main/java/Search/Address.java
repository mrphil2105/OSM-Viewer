package Search;

public record Address(String street, String houseNumber, String city, int postcode) {
    @Override
    public String toString(){
        return street + " " + houseNumber + " " + city + " " + postcode;
    }
}