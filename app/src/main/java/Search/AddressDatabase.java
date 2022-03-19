package Search;

import collections.trie.Trie;
import collections.trie.FinalTrie;
import collections.trie.TrieBuilder;
import collections.trie.TrieIterator;
import osm.OSMObserver;
import osm.elements.OSMNode;
import osm.elements.OSMTag;


import java.util.*;
import java.util.Map.Entry;
import java.util.regex.Pattern;


public class AddressDatabase implements OSMObserver {
    private Trie<Address> streetToAddress;
    private TrieBuilder<Address> trieBuilder;
    private List<Address> history;

    public AddressDatabase() {
        trieBuilder = new TrieBuilder<>('\0');
        history = getHistory();
    }

    public List<Address> getHistory(){
        return history;
    }

    public void addAddress(Address a){
        trieBuilder.put(a.street(), a);
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

        if (isAddress) addAddress(new Address(street,houseNumber,city,postcode,node));

    }

    //TODO: call from model when done parsing
    public void buildTries(){
        streetToAddress = trieBuilder.build();
    }

    public Iterator<Entry<String,Address>> streetSearch(String search){
        return streetToAddress.withPrefix(search);
    }

    private final static String REGEX = "^[ .,]*(?<street>[A-Za-zæøåÆØÅ.é ]+?)(([ .,]+)(?<house>[0-9]+[A-Za-z]?(-[0-9]+)?)([ ,.]+(?<floor>[0-9]{1,3})([ ,.]+(?<side>tv|th|mf|[0-9]{1,3})?))?([ ,.]*(?<postcode>[0-9]{4})??[ ,.]*(?<city>[A-Za-zæøåÆØÅ ]+?)?)?)?[ ,.]*$";
    private final static Pattern PATTERN = Pattern.compile(REGEX);

    public List<Address> searchAddress(String input) {
        var matcher = PATTERN.matcher(input);
        List<Address> results; //TODO: maybe an array instead


        if(matcher.matches()) {
            var street = matcher.group("street");
            var city = matcher.group("city");
            var postcode = matcher.group("postcode");
            System.out.println(street);

            if (street != null) {
                var searchedStreets = streetSearch(street);
                var parsedAddresses = new TreeSet<String>();
                results = new LinkedList<>();

                int maxEntries = 5; //TODO: it shouldn't be hardcoded
                int addedEntries = 0;

                if (city != null || postcode != null) {
                    if(postcode != null){
                        //Either build a trie with the street search results (postcodes as cities)
                        //Or build a trie of postcodes to start of with and then take intersection of the 2 results
                    } else{

                    }

                    //TODO: only add those who match the postcode
                    while (addedEntries < maxEntries && searchedStreets.hasNext()) {
                        var entry = searchedStreets.next().getValue();
                        if (parsedAddresses.add(entry.street() + "|" + entry.city())) {
                            results.add(entry);
                            //result[i] = entry;
                            addedEntries++;
                        }
                    }
                }

                while (addedEntries < maxEntries && searchedStreets.hasNext()) {
                    var entry = searchedStreets.next().getValue();
                    if (parsedAddresses.add(entry.street() + "|" + entry.city())) {
                        results.add(entry);
                        addedEntries++;
                    }
                }



                //TODO: we also need to save these
                matcher.group("house");
                matcher.group("floor");
                matcher.group("side");

                return results;
            }
        }
        return null; //if it doesn't match
    }


    //test method
    public void display(){
        for (Iterator<Entry<String, Address>> it = streetToAddress.withPrefix(""); it.hasNext(); ) {
           System.out.println(it.next().getValue().street());

        }
    }



}
