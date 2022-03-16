package Search;

import collections.trie.Trie;
import collections.trie.TrieBuilder;
import osm.OSMObserver;
import osm.elements.OSMNode;

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
    public void onNode(OSMNode node) {
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



}
