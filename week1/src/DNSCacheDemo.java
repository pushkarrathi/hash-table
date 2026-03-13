import java.util.*;

class DNSEntry {
    String ip;
    long expiryTime;

    DNSEntry(String ip, int ttlSeconds) {
        this.ip = ip;
        this.expiryTime = System.currentTimeMillis() + ttlSeconds * 1000;
    }

    boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}

class DNSCache {

    private int capacity;

    private LinkedHashMap<String, DNSEntry> cache;

    private int hits = 0;
    private int misses = 0;

    public DNSCache(int capacity) {

        this.capacity = capacity;

        cache = new LinkedHashMap<String, DNSEntry>(capacity, 0.75f, true) {
            protected boolean removeEldestEntry(Map.Entry<String, DNSEntry> eldest) {
                return size() > DNSCache.this.capacity;
            }
        };
    }

    public String resolve(String domain) {

        DNSEntry entry = cache.get(domain);

        if (entry != null && !entry.isExpired()) {
            hits++;
            System.out.println("Cache HIT → " + entry.ip);
            return entry.ip;
        }

        if (entry != null && entry.isExpired()) {
            System.out.println("Cache EXPIRED → " + domain);
            cache.remove(domain);
        }

        misses++;

        String ip = queryUpstreamDNS(domain);

        cache.put(domain, new DNSEntry(ip, 5)); // TTL = 5 seconds

        System.out.println("Cache MISS → Query upstream → " + ip);

        return ip;
    }

    private String queryUpstreamDNS(String domain) {

        try {
            Thread.sleep(100); // simulate DNS delay
        } catch (Exception e) {
        }

        Random r = new Random();

        return "172.217.14." + (100 + r.nextInt(50));
    }

    public void getCacheStats() {

        int total = hits + misses;

        double hitRate = (total == 0) ? 0 : (hits * 100.0 / total);

        System.out.println("\nCache Stats:");
        System.out.println("Hits: " + hits);
        System.out.println("Misses: " + misses);
        System.out.println("Hit Rate: " + String.format("%.2f", hitRate) + "%");
    }
}

public class DNSCacheDemo {

    public static void main(String[] args) throws Exception {

        DNSCache cache = new DNSCache(3);

        cache.resolve("google.com");
        cache.resolve("google.com");

        Thread.sleep(6000); // wait for TTL expiry

        cache.resolve("google.com");

        cache.resolve("github.com");
        cache.resolve("openai.com");
        cache.resolve("stackoverflow.com");

        cache.getCacheStats();
    }
}