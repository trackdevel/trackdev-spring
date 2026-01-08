package org.trackdev.api.configuration;

import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Simple in-memory rate limiter for login attempts.
 * Limits requests per IP/email combination to prevent brute force attacks.
 * 
 * For production deployments with multiple instances, consider using
 * Redis-based rate limiting (e.g., Bucket4j with Redis).
 */
@Component
public class RateLimiter {
    
    // Maximum login attempts allowed
    private static final int MAX_ATTEMPTS = 5;
    
    // Time window in milliseconds (15 minutes)
    private static final long WINDOW_MS = TimeUnit.MINUTES.toMillis(15);
    
    // Lockout duration after max attempts (30 minutes)
    private static final long LOCKOUT_MS = TimeUnit.MINUTES.toMillis(30);
    
    // Store: key -> [attemptCount, windowStartTime, lockedUntil]
    private final ConcurrentHashMap<String, long[]> attempts = new ConcurrentHashMap<>();
    
    /**
     * Check if a login attempt is allowed for the given key (IP or email).
     * 
     * @param key The identifier (IP address or email)
     * @return true if the attempt is allowed, false if rate limited
     */
    public boolean isAllowed(String key) {
        long now = System.currentTimeMillis();
        
        return attempts.compute(key, (k, data) -> {
            if (data == null) {
                // First attempt
                return new long[]{1, now, 0};
            }
            
            long attemptCount = data[0];
            long windowStart = data[1];
            long lockedUntil = data[2];
            
            // Check if currently locked out
            if (lockedUntil > now) {
                return data; // Still locked, return existing data
            }
            
            // Check if window has expired
            if (now - windowStart > WINDOW_MS) {
                // Reset window
                return new long[]{1, now, 0};
            }
            
            // Increment attempt count
            attemptCount++;
            
            // Check if max attempts exceeded
            if (attemptCount > MAX_ATTEMPTS) {
                // Lock out the user
                return new long[]{attemptCount, windowStart, now + LOCKOUT_MS};
            }
            
            return new long[]{attemptCount, windowStart, 0};
        })[2] <= now; // Return true if not locked (lockedUntil <= now)
    }
    
    /**
     * Get the remaining lockout time in seconds.
     * 
     * @param key The identifier (IP address or email)
     * @return Remaining lockout time in seconds, or 0 if not locked
     */
    public long getRemainingLockoutSeconds(String key) {
        long[] data = attempts.get(key);
        if (data == null) {
            return 0;
        }
        long remaining = data[2] - System.currentTimeMillis();
        return remaining > 0 ? TimeUnit.MILLISECONDS.toSeconds(remaining) : 0;
    }
    
    /**
     * Record a successful login, resetting the attempt counter.
     * 
     * @param key The identifier (IP address or email)
     */
    public void recordSuccess(String key) {
        attempts.remove(key);
    }
    
    /**
     * Get the remaining attempts before lockout.
     * 
     * @param key The identifier (IP address or email)
     * @return Number of remaining attempts
     */
    public int getRemainingAttempts(String key) {
        long[] data = attempts.get(key);
        if (data == null) {
            return MAX_ATTEMPTS;
        }
        long now = System.currentTimeMillis();
        long windowStart = data[1];
        
        // Check if window has expired
        if (now - windowStart > WINDOW_MS) {
            return MAX_ATTEMPTS;
        }
        
        return Math.max(0, MAX_ATTEMPTS - (int) data[0]);
    }
    
    /**
     * Cleanup old entries (call periodically to prevent memory leaks).
     * Entries older than 1 hour are removed.
     */
    public void cleanup() {
        long now = System.currentTimeMillis();
        long maxAge = TimeUnit.HOURS.toMillis(1);
        
        attempts.entrySet().removeIf(entry -> {
            long[] data = entry.getValue();
            long windowStart = data[1];
            long lockedUntil = data[2];
            
            // Remove if window is old and not locked
            return (now - windowStart > maxAge) && (lockedUntil < now);
        });
    }
}
