import java.util.*;

class TrieNode {
    Map<Character, TrieNode> children = new HashMap<>();
    boolean isWord = false;
    String word = null;
    int frequency = 0;
}

class AutocompleteSystem {

    private TrieNode root = new TrieNode();

    // Insert query with frequency
    public void insert(String query, int freq) {
        TrieNode node = root;
        for (char c : query.toCharArray()) {
            node = node.children.computeIfAbsent(c, k -> new TrieNode());
        }
        node.isWord = true;
        node.word = query;
        node.frequency += freq;
    }

    // Update frequency for an existing or new query
    public void updateFrequency(String query) {
        insert(query, 1);
    }

    // Get top K suggestions for a prefix
    public List<String> getSuggestions(String prefix, int k) {
        TrieNode node = root;
        for (char c : prefix.toCharArray()) {
            node = node.children.get(c);
            if (node == null) return new ArrayList<>();
        }

        PriorityQueue<TrieNode> heap = new PriorityQueue<>(
                (a, b) -> a.frequency == b.frequency ?
                        b.word.compareTo(a.word) : a.frequency - b.frequency
        );

        dfs(node, heap, k);

        List<String> res = new ArrayList<>();
        while (!heap.isEmpty()) res.add(0, heap.poll().word); // reverse order
        return res;
    }

    // DFS traversal for prefix node
    private void dfs(TrieNode node, PriorityQueue<TrieNode> heap, int k) {
        if (node.isWord) {
            heap.offer(node);
            if (heap.size() > k) heap.poll();
        }
        for (TrieNode child : node.children.values()) {
            dfs(child, heap, k);
        }
    }
}

public class AutocompleteDemo {

    public static void main(String[] args) {

        AutocompleteSystem ac = new AutocompleteSystem();

        // Sample queries with frequency
        ac.insert("java tutorial", 1234567);
        ac.insert("javascript", 987654);
        ac.insert("java download", 456789);
        ac.insert("java 21 features", 12345);
        ac.insert("javabeans example", 54321);

        // Simulate user typing prefix
        List<String> suggestions = ac.getSuggestions("jav", 10);

        System.out.println("Suggestions for prefix 'jav':");
        int rank = 1;
        for (String s : suggestions) {
            System.out.println(rank + ". " + s);
            rank++;
        }

        // Update frequency
        ac.updateFrequency("java 21 features");
        ac.updateFrequency("java 21 features");
        ac.updateFrequency("java 21 features");

        System.out.println("\nAfter updating frequency of 'java 21 features':");
        suggestions = ac.getSuggestions("jav", 10);
        rank = 1;
        for (String s : suggestions) {
            System.out.println(rank + ". " + s);
            rank++;
        }
    }
}