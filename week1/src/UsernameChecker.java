import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class UsernameChecker {

    private ConcurrentHashMap<String, Integer> usernameMap;

    private ConcurrentHashMap<String, Integer> attemptFrequency;

    public UsernameChecker() {
        usernameMap = new ConcurrentHashMap<>();
        attemptFrequency = new ConcurrentHashMap<>();
    }

    public void registerUser(String username, int userId) {
        usernameMap.put(username, userId);
    }

    public boolean checkAvailability(String username) {

        attemptFrequency.put(
                username,
                attemptFrequency.getOrDefault(username, 0) + 1
        );

        return !usernameMap.containsKey(username);
    }

    public List<String> suggestAlternatives(String username) {

        List<String> suggestions = new ArrayList<>();

        for (int i = 1; i <= 5; i++) {
            String suggestion = username + i;

            if (!usernameMap.containsKey(suggestion)) {
                suggestions.add(suggestion);
            }
        }

        String dotVersion = username.replace("_", ".");
        if (!usernameMap.containsKey(dotVersion)) {
            suggestions.add(dotVersion);
        }

        return suggestions;
    }

    public String getMostAttempted() {

        String maxUser = null;
        int maxCount = 0;

        for (Map.Entry<String, Integer> entry : attemptFrequency.entrySet()) {

            if (entry.getValue() > maxCount) {
                maxCount = entry.getValue();
                maxUser = entry.getKey();
            }
        }

        return maxUser + " (" + maxCount + " attempts)";
    }

    public static void main(String[] args) {

        UsernameChecker checker = new UsernameChecker();
        Scanner sc = new Scanner(System.in);
        checker.registerUser("john_doe", 1);
        checker.registerUser("admin", 2);
        System.out.println("Enter username to check:");
        String uname = sc.next();

        System.out.println(checker.checkAvailability("john_doe")); // false
        System.out.println(checker.checkAvailability("jane_smith")); // true
        System.out.println(checker.checkAvailability(uname));

        System.out.println(checker.suggestAlternatives("john_doe"));

        checker.checkAvailability("admin");
        checker.checkAvailability("admin");
        checker.checkAvailability("admin");

        System.out.println(checker.getMostAttempted());
    }
}