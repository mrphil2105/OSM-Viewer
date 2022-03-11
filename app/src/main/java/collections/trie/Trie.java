package collections.trie;

import java.util.Iterator;
import java.util.Map.Entry;

/** Trie */
public interface Trie<Value> {
    Iterator<Entry<String, Value>> withPrefix(String prefix);

    boolean contains(String key);

    Value get(String key);
}
