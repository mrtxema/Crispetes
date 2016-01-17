package cat.mrtxema.crispetes.service;

import java.util.HashMap;
import java.util.Map;

public class ExpirableCache<K,V> {
    private final Map<K,TimestampedValue<V>> data;
    private final long expirationTime;

    public ExpirableCache(long expirationTime) {
        this.data = new HashMap<>();
        this.expirationTime = expirationTime;
    }

    public void put(K key, V value) {
        data.put(key, new TimestampedValue<V>(value));
    }

    public V get(K key) {
        return extractValue(data.get(key));
    }

    public V remove(K key) {
        return extractValue(data.remove(key));
    }

    private boolean isExpired(TimestampedValue<V> timestampedValue) {
        return System.currentTimeMillis() - timestampedValue.getTimestamp() > expirationTime;
    }

    private V extractValue(TimestampedValue<V> timestampedValue) {
        if (timestampedValue == null || isExpired(timestampedValue)) {
            return null;
        } else {
            return timestampedValue.getValue();
        }
    }

    private static class TimestampedValue<V> {
        private final V value;
        private final long timestamp;

        public TimestampedValue(V value) {
            this.value = value;
            this.timestamp = System.currentTimeMillis();
        }

        public V getValue() {
            return value;
        }

        public long getTimestamp() {
            return timestamp;
        }
    }
}
