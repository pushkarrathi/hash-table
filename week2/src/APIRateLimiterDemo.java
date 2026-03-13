import java.util.concurrent.*;

class TokenBucket {
    private int maxTokens;
    private double tokens;
    private long lastRefillTime;
    private double refillRatePerMillis; // tokens per millisecond

    public TokenBucket(int maxTokens, long refillIntervalMillis) {
        this.maxTokens = maxTokens;
        this.tokens = maxTokens;
        this.lastRefillTime = System.currentTimeMillis();
        this.refillRatePerMillis = maxTokens * 1.0 / refillIntervalMillis;
    }

    public synchronized boolean allowRequest() {
        refill();

        if (tokens >= 1) {
            tokens -= 1;
            return true;
        }
        return false;
    }

    public synchronized int getRemainingTokens() {
        refill();
        return (int) tokens;
    }

    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTime;
        double refill = elapsed * refillRatePerMillis;
        if (refill > 0) {
            tokens = Math.min(maxTokens, tokens + refill);
            lastRefillTime = now;
        }
    }

    public synchronized long getRetryAfterMillis() {
        if (tokens >= 1) return 0;
        return (long) ((1 - tokens) / refillRatePerMillis);
    }
}

class RateLimiter {

    private ConcurrentHashMap<String, TokenBucket> clientBuckets = new ConcurrentHashMap<>();

    private int maxRequests;
    private long refillIntervalMillis; // e.g., 1 hour = 3600_000 ms

    public RateLimiter(int maxRequests, long refillIntervalMillis) {
        this.maxRequests = maxRequests;
        this.refillIntervalMillis = refillIntervalMillis;
    }

    public String checkRateLimit(String clientId) {
        clientBuckets.putIfAbsent(clientId, new TokenBucket(maxRequests, refillIntervalMillis));
        TokenBucket bucket = clientBuckets.get(clientId);

        if (bucket.allowRequest()) {
            return "Allowed (" + bucket.getRemainingTokens() + " requests remaining)";
        } else {
            long retry = bucket.getRetryAfterMillis() / 1000; // convert ms → s
            return "Denied (0 requests remaining, retry after " + retry + "s)";
        }
    }

    public String getRateLimitStatus(String clientId) {
        clientBuckets.putIfAbsent(clientId, new TokenBucket(maxRequests, refillIntervalMillis));
        TokenBucket bucket = clientBuckets.get(clientId);

        int used = maxRequests - bucket.getRemainingTokens();
        long reset = System.currentTimeMillis() + bucket.getRetryAfterMillis();
        return "{used: " + used + ", limit: " + maxRequests + ", reset: " + reset + "}";
    }
}

public class APIRateLimiterDemo {

    public static void main(String[] args) throws Exception {

        // 1000 requests per hour
        RateLimiter limiter = new RateLimiter(1000, 3600_000);

        String clientId = "abc123";

        // simulate 5 requests
        for (int i = 0; i < 5; i++) {
            System.out.println(limiter.checkRateLimit(clientId));
        }

        // simulate exceeding the limit
        for (int i = 0; i < 1000; i++) {
            limiter.checkRateLimit(clientId);
        }

        System.out.println(limiter.checkRateLimit(clientId)); // should be denied
        System.out.println(limiter.getRateLimitStatus(clientId));
    }
}