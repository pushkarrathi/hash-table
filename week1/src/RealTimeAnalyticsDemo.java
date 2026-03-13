import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

class PageViewEvent {
    String url;
    String userId;
    String source;

    public PageViewEvent(String url, String userId, String source) {
        this.url = url;
        this.userId = userId;
        this.source = source;
    }
}

class AnalyticsDashboard {

    // Page URL → total visit count
    private ConcurrentHashMap<String, AtomicInteger> pageViews = new ConcurrentHashMap<>();

    // Page URL → unique userIds
    private ConcurrentHashMap<String, Set<String>> uniqueVisitors = new ConcurrentHashMap<>();

    // Traffic source → count
    private ConcurrentHashMap<String, AtomicInteger> trafficSources = new ConcurrentHashMap<>();

    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public AnalyticsDashboard() {

        // Schedule dashboard update every 5 seconds
        scheduler.scheduleAtFixedRate(this::displayDashboard, 5, 5, TimeUnit.SECONDS);
    }

    // Process incoming page view event
    public void processEvent(PageViewEvent event) {

        // Update total page views
        pageViews.putIfAbsent(event.url, new AtomicInteger(0));
        pageViews.get(event.url).incrementAndGet();

        // Update unique visitors
        uniqueVisitors.putIfAbsent(event.url, ConcurrentHashMap.newKeySet());
        uniqueVisitors.get(event.url).add(event.userId);

        // Update traffic sources
        trafficSources.putIfAbsent(event.source, new AtomicInteger(0));
        trafficSources.get(event.source).incrementAndGet();
    }

    // Display dashboard
    private void displayDashboard() {

        System.out.println("\n=== Real-Time Dashboard ===");

        // Top 10 pages by views
        List<Map.Entry<String, AtomicInteger>> topPages = pageViews.entrySet()
                .stream()
                .sorted((a, b) -> b.getValue().get() - a.getValue().get())
                .limit(10)
                .collect(Collectors.toList());

        System.out.println("Top Pages:");
        int rank = 1;
        for (Map.Entry<String, AtomicInteger> entry : topPages) {
            String url = entry.getKey();
            int views = entry.getValue().get();
            int unique = uniqueVisitors.get(url).size();
            System.out.println(rank + ". " + url + " - " + views + " views (" + unique + " unique)");
            rank++;
        }

        // Traffic source distribution
        int totalVisits = trafficSources.values().stream().mapToInt(AtomicInteger::get).sum();
        System.out.println("\nTraffic Sources:");
        for (Map.Entry<String, AtomicInteger> entry : trafficSources.entrySet()) {
            double percent = (entry.getValue().get() * 100.0) / totalVisits;
            System.out.printf("%s: %.1f%%\n", entry.getKey(), percent);
        }

        System.out.println("===========================\n");
    }
}

public class RealTimeAnalyticsDemo {

    public static void main(String[] args) throws Exception {

        AnalyticsDashboard dashboard = new AnalyticsDashboard();

        // Simulate incoming events
        String[] pages = {"/article/breaking-news", "/sports/championship", "/tech/gadgets"};
        String[] sources = {"Google", "Facebook", "Direct", "Twitter"};

        Random rand = new Random();

        // Generate 100 random events per second for demonstration
        ScheduledExecutorService eventGenerator = Executors.newScheduledThreadPool(1);
        eventGenerator.scheduleAtFixedRate(() -> {
            for (int i = 0; i < 100; i++) {
                String url = pages[rand.nextInt(pages.length)];
                String source = sources[rand.nextInt(sources.length)];
                String userId = "user_" + rand.nextInt(5000);
                dashboard.processEvent(new PageViewEvent(url, userId, source));
            }
        }, 0, 1, TimeUnit.SECONDS);

        // Keep program running
        Thread.sleep(20000); // run demo for 20 seconds
        eventGenerator.shutdown();
        dashboard.scheduler.shutdown();
    }
}