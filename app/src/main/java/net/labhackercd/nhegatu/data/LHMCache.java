package net.labhackercd.nhegatu.data;

import java.util.LinkedHashMap;

/**
 * Poor man's in-memory cache.
 */
public class LHMCache extends Cache {
    private LinkedHashMap<Object, Object> cache = new LinkedHashMap<>();

    @SuppressWarnings("unchecked")
    protected <T> T get(Object key) {
        try {
            return (T) cache.get(key);
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    protected <T> void put(Object key, T value) {
        cache.put(key, value);
    }
}
