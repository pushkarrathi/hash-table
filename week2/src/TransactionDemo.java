import java.util.*;
import java.time.*;

class Transaction {
    int id;
    int amount;
    String merchant;
    String account;
    LocalDateTime time;

    public Transaction(int id, int amount, String merchant, String account, String timeStr) {
        this.id = id;
        this.amount = amount;
        this.merchant = merchant;
        this.account = account;
        this.time = LocalDateTime.parse(timeStr); // ISO_LOCAL_DATE_TIME format e.g., "2026-03-13T10:00"
    }

    @Override
    public String toString() {
        return "id:" + id;
    }
}

class TransactionAnalyzer {

    List<Transaction> transactions;

    public TransactionAnalyzer(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    // Classic Two-Sum
    public List<List<Transaction>> findTwoSum(int target) {
        List<List<Transaction>> result = new ArrayList<>();
        Map<Integer, Transaction> map = new HashMap<>();

        for (Transaction t : transactions) {
            int complement = target - t.amount;
            if (map.containsKey(complement)) {
                result.add(Arrays.asList(map.get(complement), t));
            }
            map.put(t.amount, t);
        }
        return result;
    }

    // Two-Sum within time window (in minutes)
    public List<List<Transaction>> findTwoSumInWindow(int target, int windowMinutes) {
        List<List<Transaction>> result = new ArrayList<>();
        transactions.sort(Comparator.comparing(t -> t.time));

        Map<Integer, List<Transaction>> map = new HashMap<>();
        for (Transaction t : transactions) {

            int complement = target - t.amount;
            if (map.containsKey(complement)) {
                for (Transaction t2 : map.get(complement)) {
                    if (Math.abs(Duration.between(t.time, t2.time).toMinutes()) <= windowMinutes) {
                        result.add(Arrays.asList(t2, t));
                    }
                }
            }
            map.computeIfAbsent(t.amount, k -> new ArrayList<>()).add(t);
        }
        return result;
    }

    // K-Sum (recursive)
    public List<List<Transaction>> findKSum(int k, int target) {
        List<List<Transaction>> result = new ArrayList<>();
        findKSumHelper(transactions, 0, k, target, new ArrayList<>(), result);
        return result;
    }

    private void findKSumHelper(List<Transaction> trans, int start, int k, int target,
                                List<Transaction> path, List<List<Transaction>> res) {
        if (k == 0) {
            if (target == 0) res.add(new ArrayList<>(path));
            return;
        }
        for (int i = start; i < trans.size(); i++) {
            path.add(trans.get(i));
            findKSumHelper(trans, i + 1, k - 1, target - trans.get(i).amount, path, res);
            path.remove(path.size() - 1);
        }
    }

    // Duplicate detection: same amount & merchant, different accounts
    public List<Map<String, Object>> detectDuplicates() {
        List<Map<String, Object>> duplicates = new ArrayList<>();
        Map<String, List<String>> map = new HashMap<>(); // key = amount|merchant

        for (Transaction t : transactions) {
            String key = t.amount + "|" + t.merchant;
            map.computeIfAbsent(key, k -> new ArrayList<>()).add(t.account);
        }

        for (String key : map.keySet()) {
            List<String> accounts = map.get(key);
            Set<String> unique = new HashSet<>(accounts);
            if (unique.size() > 1) {
                String[] parts = key.split("\\|");
                Map<String, Object> entry = new HashMap<>();
                entry.put("amount", Integer.parseInt(parts[0]));
                entry.put("merchant", parts[1]);
                entry.put("accounts", unique);
                duplicates.add(entry);
            }
        }
        return duplicates;
    }
}

public class TransactionDemo {

    public static void main(String[] args) {

        List<Transaction> txs = Arrays.asList(
                new Transaction(1, 500, "Store A", "acc1", "2026-03-13T10:00"),
                new Transaction(2, 300, "Store B", "acc2", "2026-03-13T10:15"),
                new Transaction(3, 200, "Store C", "acc3", "2026-03-13T10:30"),
                new Transaction(4, 500, "Store A", "acc2", "2026-03-13T11:00")
        );

        TransactionAnalyzer analyzer = new TransactionAnalyzer(txs);

        System.out.println("=== Classic Two-Sum (target 500) ===");
        for (List<Transaction> pair : analyzer.findTwoSum(500)) {
            System.out.println(pair);
        }

        System.out.println("\n=== Two-Sum within 60 minutes ===");
        for (List<Transaction> pair : analyzer.findTwoSumInWindow(500, 60)) {
            System.out.println(pair);
        }

        System.out.println("\n=== K-Sum (k=3, target=1000) ===");
        for (List<Transaction> combo : analyzer.findKSum(3, 1000)) {
            System.out.println(combo);
        }

        System.out.println("\n=== Duplicate Detection ===");
        for (Map<String, Object> dup : analyzer.detectDuplicates()) {
            System.out.println(dup);
        }
    }
}