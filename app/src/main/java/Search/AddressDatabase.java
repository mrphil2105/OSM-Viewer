package Search;

import collections.trie.Trie;
import collections.trie.FinalTrie;
import collections.trie.TrieBuilder;
import collections.trie.TrieIterator;
import osm.OSMObserver;
import osm.elements.OSMNode;

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
        trieBuilder.put(a.street() + " " + a.houseNumber(), a);
    }

    @Override
    public void onNode(OSMNode node) {
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
        List<Address> results;

        if (!matcher.matches()) {
            return null;
        }

        var street = matcher.group("street");
        var searchedStreets = streetSearch(street);
        var parsedAddresses = new TreeSet<String>();
        results = new LinkedList<>();

        int entriesAdded = 0;
        int maxEntries = 5; //TODO: it shouldnt be hardcoded
        while(searchedStreets.hasNext() && entriesAdded < maxEntries){
            entriesAdded++;
            var entry = searchedStreets.next().getValue();

            String city = entry.city() == null ? "" : entry.city();

            if(parsedAddresses.add(entry.street() + city)){
                results.add(entry);
            }
        }

        //TODO: we also need to save these
        matcher.group("house");
        matcher.group("floor");
        matcher.group("side");

        return results;
    }




    //Temporary test method
    public static void main(String[] args) {
        var db = new AddressDatabase();

        var sv = new Address("Svend","3","SOlrød",2680);
        var sve = new Address("Svend","4","SOlrød",2680);
        var svend = new Address("Svend","2","SOlrød",2680);
        var rued = new Address("Rued","2","SOlrød",2680);
        Address[] addresses = new Address[]{sv, sve, svend, rued}; //TODO
        
        Arrays.stream(addresses).forEach(db::addAddress);

        db.buildTries();

        Iterator<Entry<String, Address>> result = db.streetSearch("Sv");

        System.out.println("FIRST");
        while(result.hasNext()){
            var element = result.next();
            System.out.println(element.getValue().street());
        }


        /*
        var result2 = result.withPrefixTest("e");
        if(result2 != null){
            System.out.println("SECOND");
            var iterator2 = result2.iterator();
            while(iterator2.hasNext()){
                var element = iterator2.next();
                System.out.println(element.getValue().street());
            }
        }
         */
    }



}
