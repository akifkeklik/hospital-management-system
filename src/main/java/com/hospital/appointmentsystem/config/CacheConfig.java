package com.hospital.appointmentsystem.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.concurrent.Callable;

@Configuration
@EnableCaching
public class CacheConfig {

    private static final Logger log = LoggerFactory.getLogger(CacheConfig.class);

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager() {
            @Override
            protected Cache createConcurrentMapCache(String name) {
                return new LoggingCache(super.createConcurrentMapCache(name));
            }
        };
        cacheManager.setCacheNames(Arrays.asList("departments", "polyclinics", "doctors", "systemSettings"));
        return cacheManager;
    }

    /**
     * A decorator to log cache operations transparently.
     */
    private static class LoggingCache implements Cache {

        private final Cache delegate;

        public LoggingCache(Cache delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getName() {
            return delegate.getName();
        }

        @Override
        public Object getNativeCache() {
            return delegate.getNativeCache();
        }

        @Override
        public ValueWrapper get(Object key) {
            ValueWrapper valueWrapper = delegate.get(key);
            if (valueWrapper == null) {
                log.debug("[Cache MISS] {} - key: {}", getName(), key);
            } else {
                log.debug("[Cache HIT] {} - key: {}", getName(), key);
            }
            return valueWrapper;
        }

        @Override
        public <T> T get(Object key, Class<T> type) {
            T value = delegate.get(key, type);
            if (value == null) {
                log.debug("[Cache MISS] {} - key: {}", getName(), key);
            } else {
                log.debug("[Cache HIT] {} - key: {}", getName(), key);
            }
            return value;
        }

        @Override
        public <T> T get(Object key, Callable<T> valueLoader) {
            log.debug("[Cache ACCESS] {} - key: {} (valueLoader)", getName(), key);
            return delegate.get(key, valueLoader);
        }

        @Override
        public void put(Object key, Object value) {
            log.debug("[Cache PUT] {} - key: {}", getName(), key);
            delegate.put(key, value);
        }

        @Override
        public void evict(Object key) {
            log.debug("[Cache EVICT] {} - key: {}", getName(), key);
            delegate.evict(key);
        }

        @Override
        public void clear() {
            log.debug("[Cache CLEAR] {}", getName());
            delegate.clear();
        }
    }
}
