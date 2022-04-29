package Search;

import collections.trie.Trie;
import collections.trie.TrieBuilder;
import java.io.Serializable;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import osm.OSMObserver;
import osm.elements.OSMNode;
import osm.elements.OSMTag;
import util.Predicates;

public class AddressDatabase implements OSMObserver, Serializable {
    private Trie<List<Address>> streetToAddress;
    private Trie<List<Address>> cityToAddress;
    private Trie<List<Address>> postcodeToAddress;
    private TrieBuilder<List<Address>> streetTrieBuilder;
    private TrieBuilder<List<Address>> cityTrieBuilder;
    private TrieBuilder<List<Address>> postcodeTrieBuilder;
    private final List<Address> history;

    public AddressDatabase() {
        streetTrieBuilder = new TrieBuilder<>('\0');
        cityTrieBuilder = new TrieBuilder<>('\0');
        postcodeTrieBuilder = new TrieBuilder<>('\0');
        history = new ArrayList<>();
    }

    public static AddressBuilder parse(String toParse) {
        var builder = new AddressBuilder();
        var matcher = PATTERN.matcher(toParse);

        if (matcher.matches()) {
            builder.street(matcher.group("street"));
            builder.house(matcher.group("house"));
            builder.postcode(matcher.group("postcode"));
            builder.city(matcher.group("city"));
            builder.floor(matcher.group("floor"));
            builder.side(matcher.group("side"));
        } else {
            return null;
        }
        return builder;
    }

    public List<Address> getHistory() {
        return history;
    }

    public void addAddress(Address a) {
        addToTrie(streetTrieBuilder, a.street(), a);
        if (a.city() != null) {
            addToTrie(cityTrieBuilder, a.city(), a);
        }
        if (a.postcode() != null) {
            addToTrie(postcodeTrieBuilder, a.postcode(), a);
        }
    }

    private void addToTrie(TrieBuilder<List<Address>> trie, String key, Address value) {
        var set = trie.get(key);
        if (set == null) {
            set = new ArrayList<>();
        }
        set.add(value);
        trie.put(key, set);
    }

    @Override
    public void onNode(OSMNode node) {
        var builder = new AddressBuilder();

        boolean isAddress = false;

        for (OSMTag t : node.tags()) {
            if (t.key() == OSMTag.Key.STREET) {
                builder.street(t.value());
                isAddress = true;
            }
            if (t.key() == OSMTag.Key.HOUSENUMBER) {
                builder.house(t.value());
            }
            if (t.key() == OSMTag.Key.CITY) {
                builder.city(t.value());
            }
            if (t.key() == OSMTag.Key.POSTCODE) {
                builder.postcode(t.value());
            }
        }

        builder.lat((float) node.lat());
        builder.lon((float) node.lon());

        if (isAddress) addAddress(builder.build());
    }

    @Override
    public void onFinish() {
        streetToAddress = streetTrieBuilder.build();
        cityToAddress = cityTrieBuilder.build();
        postcodeToAddress = postcodeTrieBuilder.build();
        postcodeTrieBuilder = null;
        streetTrieBuilder = null;
        cityTrieBuilder = null;
    }

    private static final String REGEX =
            "^[ .,]*(?<street>[A-Za-zæøåÆØÅ.é ]+?)(([ .,]+)(?<house>[0-9]+[A-Za-z]?(-[0-9]+)?)([ ,.]+(?<floor>[0-9]{1,3})([ ,.]+(?<side>tv|th|mf|[0-9]{1,3})?))?([ ,.]*(?<postcode>[0-9]{4})??[ ,.]*(?<city>[A-Za-zæøåÆØÅ ]+?)?)?)?[ ,.]*$";
    private static final Pattern PATTERN = Pattern.compile(REGEX);

    public List<Address> search(Address input) {

        var searchedStreets = streetToAddress.get(input.street());
        if (searchedStreets == null) return null;

        var filterStream = searchedStreets.stream();

        if (input.postcode() != null) {
            filterStream = filterStream.filter(e -> e.postcode().equals(input.postcode()));
        }
        if (input.city() != null) {
            filterStream = filterStream.filter(e -> e.city().equals(input.city()));
        }
        if (input.houseNumber() != null) {
            filterStream = filterStream.filter(e -> e.houseNumber().equals(input.houseNumber()));
        }
        return filterStream.toList();
    }

    public List<Address> possibleAddresses(Address input, int maxEntries) {
        Stream<Address> filterStream =
                StreamSupport.stream(
                                Spliterators.spliteratorUnknownSize(
                                        streetToAddress.withPrefix(input.street()), Spliterator.ORDERED),
                                false)
                        .flatMap(e -> e.getValue().stream());

        if (input.houseNumber() != null) {
            filterStream = filterStream.filter(e -> e.houseNumber().startsWith(input.houseNumber()));
        }

        if (input.postcode() != null) {
            var searchedPostcodes = postcodeToAddress.withPrefix(input.postcode());

            var retain = new HashSet<Address>();
            while (searchedPostcodes.hasNext()) {
                var entry = searchedPostcodes.next().getValue();
                retain.addAll(entry);
            }

            filterStream = filterStream.filter(retain::contains);
        }

        if (input.city() != null) {
            var searchedCities = cityToAddress.withPrefix(input.city());
            var retain = new HashSet<Address>();
            while (searchedCities.hasNext()) {
                var entry = searchedCities.next().getValue();
                retain.addAll(entry);
            }

            filterStream = filterStream.filter(retain::contains);
        }

        if (input.houseNumber() != null) {
            return filterStream.limit(maxEntries).toList();
        } else {
            return filterStream
                    .filter(
                            Predicates.distinctByKey(e -> e.street().hashCode() * e.postcode().hashCode()))
                    .limit(maxEntries)
                    .toList();
        }

    }
}
