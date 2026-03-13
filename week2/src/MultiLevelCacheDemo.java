import java.util.*;

class VideoData {
    String videoId;
    String content;

    public VideoData(String videoId, String content) {
        this.videoId = videoId;
        this.content = content;
    }
}

// L1: In-memory LRU Cache
class L1Cache extends LinkedHashMap<String, VideoData> {
    private final int capacity;
    int hits = 0;
    int requests = 0;
    long totalTime = 0;

    public L1Cache(int capacity) {
        super(capacity, 0.75f, true); // access-order
        this.capacity = capacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<String, VideoData> eldest) {
        return size() > capacity;
    }

    public VideoData getVideo(String videoId) {
        requests++;
        VideoData data = super.get(videoId);
        if (data != null) hits++;
        totalTime += 0.5; // 0.5ms for L1
        return data;
    }

    public void putVideo(String videoId, VideoData data) {
        super.put(videoId, data);
    }
}

// L2: SSD-backed simulation (HashMap + access count for promotion)
class L2Cache {
    Map<String, VideoData> store = new HashMap<>();
    Map<String, Integer> accessCount = new HashMap<>();
    int capacity;
    int hits = 0;
    int requests = 0;
    long totalTime = 0;

    public L2Cache(int capacity) {
        this.capacity = capacity;
    }

    public VideoData getVideo(String videoId) {
        requests++;
        totalTime += 5; // 5ms for L2
        if (store.containsKey(videoId)) {
            hits++;
            accessCount.put(videoId, accessCount.getOrDefault(videoId, 0) + 1);
            return store.get(videoId);
        }
        return null;
    }

    public void putVideo(String videoId, VideoData data) {
        if (store.size() >= capacity) {
            // simple eviction: remove random or LRU simulation
            String firstKey = store.keySet().iterator().next();
            store.remove(firstKey);
            accessCount.remove(firstKey);
        }
        store.put(videoId, data);
        accessCount.put(videoId, 1);
    }

    public int getAccessCount(String videoId) {
        return accessCount.getOrDefault(videoId, 0);
    }
}

// L3: Database simulation
class L3Database {
    Map<String, VideoData> db = new HashMap<>();
    int hits = 0;
    int requests = 0;
    long totalTime = 0;

    public VideoData getVideo(String videoId) {
        requests++;
        totalTime += 150; // 150ms for DB
        if (db.containsKey(videoId)) {
            hits++;
            return db.get(videoId);
        }
        return null;
    }

    public void addVideo(VideoData data) {
        db.put(data.videoId, data);
    }
}

// Multi-Level Cache System
class MultiLevelCache {

    L1Cache l1;
    L2Cache l2;
    L3Database l3;
    int promotionThreshold;

    public MultiLevelCache(int l1Cap, int l2Cap, int promotionThreshold) {
        this.l1 = new L1Cache(l1Cap);
        this.l2 = new L2Cache(l2Cap);
        this.l3 = new L3Database();
        this.promotionThreshold = promotionThreshold;
    }

    public VideoData getVideo(String videoId) {
        VideoData data = l1.getVideo(videoId);
        if (data != null) {
            System.out.println("L1 Cache HIT (" + 0.5 + "ms)");
            return data;
        }
        System.out.println("L1 Cache MISS");

        data = l2.getVideo(videoId);
        if (data != null) {
            System.out.println("L2 Cache HIT (5ms)");
            // Promote to L1 if access count exceeds threshold
            if (l2.getAccessCount(videoId) >= promotionThreshold) {
                l1.putVideo(videoId, data);
                System.out.println("Promoted to L1");
            }
            return data;
        }
        System.out.println("L2 Cache MISS");

        data = l3.getVideo(videoId);
        if (data != null) {
            System.out.println("L3 Database HIT (150ms)");
            // Add to L2
            l2.putVideo(videoId, data);
            return data;
        }

        System.out.println("Video not found in L3");
        return null;
    }

    public void addVideoToDB(VideoData data) {
        l3.addVideo(data);
    }

    public void getStatistics() {
        double l1HitRate = l1.requests == 0 ? 0 : l1.hits * 100.0 / l1.requests;
        double l2HitRate = l2.requests == 0 ? 0 : l2.hits * 100.0 / l2.requests;
        double l3HitRate = l3.requests == 0 ? 0 : l3.hits * 100.0 / l3.requests;

        double l1Avg = l1.requests == 0 ? 0 : l1.totalTime * 1.0 / l1.requests;
        double l2Avg = l2.requests == 0 ? 0 : l2.totalTime * 1.0 / l2.requests;
        double l3Avg = l3.requests == 0 ? 0 : l3.totalTime * 1.0 / l3.requests;

        double overallHitRate = (l1.hits + l2.hits + l3.hits) * 100.0 / (l1.requests + l2.requests + l3.requests);
        double overallAvgTime = (l1.totalTime + l2.totalTime + l3.totalTime) * 1.0 / (l1.requests + l2.requests + l3.requests);

        System.out.printf("L1: Hit Rate %.2f%%, Avg Time: %.2fms\n", l1HitRate, l1Avg);
        System.out.printf("L2: Hit Rate %.2f%%, Avg Time: %.2fms\n", l2HitRate, l2Avg);
        System.out.printf("L3: Hit Rate %.2f%%, Avg Time: %.2fms\n", l3HitRate, l3Avg);
        System.out.printf("Overall: Hit Rate %.2f%%, Avg Time: %.2fms\n", overallHitRate, overallAvgTime);
    }
}

public class MultiLevelCacheDemo {

    public static void main(String[] args) {

        MultiLevelCache cacheSystem = new MultiLevelCache(10000, 100000, 3);

        // Add videos to DB
        for (int i = 1; i <= 5; i++) {
            cacheSystem.addVideoToDB(new VideoData("video_" + i, "Content_" + i));
        }

        System.out.println("=== First Request for video_1 ===");
        cacheSystem.getVideo("video_1"); // L1 miss, L2 miss, L3 hit → promoted to L2

        System.out.println("\n=== Second Request for video_1 ===");
        cacheSystem.getVideo("video_1"); // L1 miss, L2 hit → count 2, not promoted yet

        System.out.println("\n=== Third Request for video_1 ===");
        cacheSystem.getVideo("video_1"); // L2 hit → count 3 → promoted to L1

        System.out.println("\n=== Request for video_5 ===");
        cacheSystem.getVideo("video_5"); // L1 miss, L2 miss, L3 hit → promoted to L2

        System.out.println("\n=== Cache Statistics ===");
        cacheSystem.getStatistics();
    }
}