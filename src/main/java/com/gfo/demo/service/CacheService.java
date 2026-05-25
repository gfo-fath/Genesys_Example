package com.gfo.demo.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class CacheService {

    // In-memory cache for demo purposes (replace with RedisTemplate when Redis is available)
    private final Map<String, Object> cache = new ConcurrentHashMap<>();
    private final Map<String, Long> expirationTimes = new ConcurrentHashMap<>();

    public void put(String key, Object value) {
        cache.put(key, value);
        // No expiration for demo
    }

    public void put(String key, Object value, long timeout, TimeUnit timeUnit) {
        cache.put(key, value);
        long expirationTime = System.currentTimeMillis() + timeUnit.toMillis(timeout);
        expirationTimes.put(key, expirationTime);
    }

    public Object get(String key) {
        Long expirationTime = expirationTimes.get(key);
        if (expirationTime != null && System.currentTimeMillis() > expirationTime) {
            cache.remove(key);
            expirationTimes.remove(key);
            return null;
        }
        return cache.get(key);
    }

    public void delete(String key) {
        cache.remove(key);
        expirationTimes.remove(key);
    }

    public Boolean hasKey(String key) {
        return cache.containsKey(key);
    }

    public void expire(String key, long timeout, TimeUnit timeUnit) {
        long expirationTime = System.currentTimeMillis() + timeUnit.toMillis(timeout);
        expirationTimes.put(key, expirationTime);
    }

    // Customer-specific cache methods
    public void cacheCustomer(String customerId, Object customer) {
        put("customer:" + customerId, customer, 30, TimeUnit.MINUTES);
    }

    public Object getCachedCustomer(String customerId) {
        return get("customer:" + customerId);
    }

    public void evictCustomerCache(String customerId) {
        delete("customer:" + customerId);
    }

    // Interaction-specific cache methods
    public void cacheInteraction(String interactionId, Object interaction) {
        put("interaction:" + interactionId, interaction, 15, TimeUnit.MINUTES);
    }

    public Object getCachedInteraction(String interactionId) {
        return get("interaction:" + interactionId);
    }

    public void evictInteractionCache(String interactionId) {
        delete("interaction:" + interactionId);
    }

    // Agent status cache
    public void cacheAgentStatus(String agentId, String status) {
        put("agent:status:" + agentId, status, 2, TimeUnit.HOURS);
    }

    public String getCachedAgentStatus(String agentId) {
        return (String) get("agent:status:" + agentId);
    }

    // Queue statistics cache
    public void cacheQueueStats(String queueName, Object stats) {
        put("queue:stats:" + queueName, stats, 5, TimeUnit.MINUTES);
    }

    public Object getCachedQueueStats(String queueName) {
        return get("queue:stats:" + queueName);
    }

    // Clear all cache (for demo purposes)
    public void clearAllCache() {
        cache.clear();
        expirationTimes.clear();
    }
}