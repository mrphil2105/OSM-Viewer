package Search;

import collections.trie.Trie;
import collections.trie.TrieBuilder;
import osm.OSMObserver;
import osm.elements.OSMNode;
import osm.elements.OSMTag;


import java.util.Arrays;
import java.util.Iterator;
import java.util.Map.Entry;


public class AddressDatabase implements OSMObserver {
    Trie<Address> streetToAddress;
    TrieBuilder<Address> trieBuilder;


    public AddressDatabase() {
        trieBuilder = new TrieBuilder<>('\0');

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



    public Iterator<Entry<String,Address>> search(String search){
        return streetToAddress.withPrefix("Sv");
    }

    //Temporary test method
    public static void main(String[] args) {
        var db = new AddressDatabase();

        Address[] addresses = new Address[2]; //TODO
        
        Arrays.stream(addresses).forEach(db::addAddress);

        var results = db.search("Sv");

        while(results.hasNext()){
            var element = results.next();
            System.out.println(element.getValue().street());
        }
    }

    public void display(){
        for (Iterator<Entry<String, Address>> it = streetToAddress.withPrefix(""); it.hasNext(); ) {
           System.out.println(it.next().getValue().street());

        }
    }



}
