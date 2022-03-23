package Search;

import collections.trie.Trie;
import collections.trie.FinalTrie;
import collections.trie.TrieBuilder;
import collections.trie.TrieIterator;
import osm.OSMObserver;
import osm.elements.OSMNode;
import osm.elements.OSMTag;


import java.io.Serializable;
import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;


public class AddressDatabase implements OSMObserver, Serializable {
    private Trie<Address> addressTrie;
    private TrieBuilder<Address> addressTrieBuilder;


    private List<Address> history;


    public AddressDatabase() {
        addressTrieBuilder = new TrieBuilder<>('\0');
        history = getHistory();
    }

    public List<Address> getHistory(){
        return history;
    }

    public void addAddress(Address a){
        addressTrieBuilder.put(a.toString(), a);
    }

    @Override
    public void onNode(OSMNode node){
        String street = "";
        String houseNumber = "";
        String city = "";
        int postcode = 0;

        boolean isAddress = false;

        for (OSMTag t : node.tags()){
            if (t.key()==OSMTag.Key.STREET){
                street=t.value();
                isAddress=true;
            }
            if (t.key()==OSMTag.Key.HOUSENUMBER){
                houseNumber=t.value();
            }
            if (t.key()==OSMTag.Key.CITY){
                city=t.value();
            }
            if (t.key()==OSMTag.Key.POSTCODE){
                postcode= Integer.parseInt(t.value());
            }
        }

        if (isAddress) addAddress(new Address(street,houseNumber,city,postcode,node.slim()));

    }


    public void buildTries(){
       addressTrie = addressTrieBuilder.build();
    }

    public Iterator<Entry<String,Address>> streetSearch(String search){
        return addressTrie.withPrefix(search);
    }

    private final static String REGEX = "^[ .,]*(?<street>[A-Za-zæøåÆØÅ.é ]+?)(([ .,]+)(?<house>[0-9]+[A-Za-z]?(-[0-9]+)?)([ ,.]+(?<floor>[0-9]{1,3})([ ,.]+(?<side>tv|th|mf|[0-9]{1,3})?))?([ ,.]*(?<postcode>[0-9]{4})??[ ,.]*(?<city>[A-Za-zæøåÆØÅ ]+?)?)?)?[ ,.]*$";
    private final static Pattern PATTERN = Pattern.compile(REGEX);

    public List<Address> searchAddress(String input) {
        var matcher = PATTERN.matcher(input);
        List<Address> results; //TODO: maybe an array instead


        if(matcher.matches()) {
            var street= (matcher.group("street")!=null) ? matcher.group("street") : "";
            var house= (matcher.group("house")!=null) ? matcher.group("house") : "";
            var city= (matcher.group("city")!=null) ? matcher.group("city") : "";
            var postcode= (matcher.group("postcode")!=null) ? matcher.group("postcode") : "";

            results = new LinkedList<Address>();

            int maxEntries = 5; //TODO: it shouldn't be hardcoded
            int addedEntries = 0;




            if (house.length()>0) {

                var possibleAddresses = streetSearch(street + " " + house + " "  + city + " " + postcode);

                for (Iterator<Entry<String, Address>> it = possibleAddresses; it.hasNext() && (addedEntries<maxEntries); ) {
                    results.add(it.next().getValue());
                    addedEntries++;
                }

                return results;
            }
            else {
               var possibleAddresses = streetSearch(street);
               Set<String> parsedStreets = new HashSet<>();

                while (addedEntries < maxEntries && possibleAddresses.hasNext()) {

                    var entry = possibleAddresses.next().getValue();
                    if (entry.city().startsWith(city) && parsedStreets.add(entry.street() + "|" + entry.city())){
                        results.add(entry);
                        addedEntries++;
                    }

                }
                return results;
            }


        }
        return null; //if it doesn't match
    }


    //test method
    public void display(){

        for (Iterator<Entry<String, Address>> it = addressTrie.withPrefix(""); it.hasNext(); ) {
            System.out.println(it.next().getValue().toString());

        }



    }



}
