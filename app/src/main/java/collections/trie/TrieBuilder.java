package collections.trie;

/**
 * TrieBuilder is an interface for creating FinalTries.
 *
 * @param <Value> The type that can be looked up using String keys.
 */
public class TrieBuilder<Value> {
    char key;
    Value value;
    int childrenIdx;
    TrieBuilder<Value>[] children;

    @SuppressWarnings("unchecked")
    public TrieBuilder(int radix) {
        children = new TrieBuilder[radix];
    }

    @SuppressWarnings("unchecked")
    public FinalTrie<Value> build() {
        var trie = new FinalTrie<Value>();
        trie.key = key;
        trie.value = value;
        trie.children = new FinalTrie[childrenIdx];

        for (int i = 0; i < childrenIdx; i++) {
            trie.children[i] = children[i].build();
        }

        return trie;
    }

    public void put(String key, Value value) {
        if (value == null) throw new IllegalArgumentException("value must not be null");

        put(key, 0, value);
    }

    void put(String key, int idx, Value value) {
        var child = getChild(key.charAt(idx));

        if (child == null) {
            child = new TrieBuilder<Value>(children.length);
            child.key = key.charAt(idx);
            addChild(child);
        }

        if (idx == key.length() - 1) {
            child.value = value;
        } else {
            child.put(key, idx + 1, value);
        }
    }

    void addChild(TrieBuilder<Value> child) {
        // Assume we aren't adding beyond the radix.
        // I don't care to handle this case.
        children[childrenIdx++] = child;
    }

    TrieBuilder<Value> getChild(char c) {
        for (int i = 0; i < childrenIdx; i++) {
            if (children[i].key == c) {
                return children[i];
            }
        }

        return null;
    }
}
