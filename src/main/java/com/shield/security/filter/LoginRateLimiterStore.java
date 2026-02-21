package com.shield.security.filter;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LoginRateLimiterStore {

    private static final int CLEANUP_INTERVAL = 200;

    private final JdbcTemplate jdbcTemplate;
    private final AtomicInteger cleanupCounter = new AtomicInteger(0);

    public LoginRateLimiterStore(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int incrementAndGet(String bucketKey, Instant windowStart) {
        maybeCleanup(windowStart);
        Integer count = jdbcTemplate.queryForObject("""
                        INSERT INTO login_rate_limit_bucket (bucket_key, request_count, window_start)
                        VALUES (?, 1, ?)
                        ON CONFLICT (bucket_key)
                        DO UPDATE SET request_count = login_rate_limit_bucket.request_count + 1
                        RETURNING request_count
                        """,
                Integer.class,
                bucketKey,
                Timestamp.from(windowStart));
        return count == null ? 0 : count;
    }

    private void maybeCleanup(Instant windowStart) {
        if (cleanupCounter.incrementAndGet() % CLEANUP_INTERVAL != 0) {
            return;
        }
        Instant threshold = windowStart.minusSeconds(24 * 60 * 60L);
        jdbcTemplate.update("DELETE FROM login_rate_limit_bucket WHERE window_start < ?", Timestamp.from(threshold));
    }
}
