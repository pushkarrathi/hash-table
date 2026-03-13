import java.util.*;

class PlagiarismDetector {

    // ngram -> set of document names
    private HashMap<String, Set<String>> index = new HashMap<>();

    // document -> its ngrams
    private HashMap<String, Set<String>> documentNgrams = new HashMap<>();

    private int N = 5; // size of n-gram

    // Add document to database
    public void addDocument(String docName, String text) {

        Set<String> ngrams = generateNgrams(text);
        documentNgrams.put(docName, ngrams);

        for (String gram : ngrams) {

            index.putIfAbsent(gram, new HashSet<>());
            index.get(gram).add(docName);
        }

        System.out.println("Indexed document: " + docName + " (" + ngrams.size() + " n-grams)");
    }

    // Analyze a new document
    public void analyzeDocument(String docName, String text) {

        Set<String> newNgrams = generateNgrams(text);

        System.out.println("\nAnalyzing: " + docName);
        System.out.println("Extracted " + newNgrams.size() + " n-grams");

        HashMap<String, Integer> matchCount = new HashMap<>();

        for (String gram : newNgrams) {

            if (index.containsKey(gram)) {

                for (String existingDoc : index.get(gram)) {

                    matchCount.put(existingDoc,
                            matchCount.getOrDefault(existingDoc, 0) + 1);
                }
            }
        }

        for (String doc : matchCount.keySet()) {

            int matches = matchCount.get(doc);

            double similarity =
                    (matches * 100.0) / newNgrams.size();

            System.out.println("Matches with " + doc + ": "
                    + matches + " n-grams");

            System.out.printf("Similarity: %.2f%%\n", similarity);

            if (similarity > 50) {
                System.out.println("PLAGIARISM DETECTED!");
            } else if (similarity > 10) {
                System.out.println("Suspicious similarity.");
            }

            System.out.println();
        }
    }

    // Generate n-grams
    private Set<String> generateNgrams(String text) {

        String[] words = text.toLowerCase().split("\\s+");

        Set<String> grams = new HashSet<>();

        for (int i = 0; i <= words.length - N; i++) {

            StringBuilder gram = new StringBuilder();

            for (int j = 0; j < N; j++) {
                gram.append(words[i + j]).append(" ");
            }

            grams.add(gram.toString().trim());
        }

        return grams;
    }
}

public class PlagiarismSystem {

    public static void main(String[] args) {

        PlagiarismDetector detector = new PlagiarismDetector();

        String essay1 =
                "Artificial intelligence is transforming modern technology and changing how people interact with computers.";

        String essay2 =
                "Artificial intelligence is transforming modern technology and changing the way humans interact with machines.";

        String essay3 =
                "Climate change is one of the biggest global challenges that scientists are studying today.";

        detector.addDocument("essay_089.txt", essay1);
        detector.addDocument("essay_092.txt", essay2);

        detector.analyzeDocument("essay_123.txt", essay3);
    }
}