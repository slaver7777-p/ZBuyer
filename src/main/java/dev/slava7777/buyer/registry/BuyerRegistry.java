package dev.slava7777.buyer.registry;

import dev.slava7777.buyer.util.CyclicListIterator;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.*;

public class BuyerRegistry<T> implements Iterable<T> {
    private final Map<String, T> id2value = new LinkedHashMap<>();
    private final Map<T, String> value2id = new IdentityHashMap<>();
    private boolean writeLock;
    private T first;

    private List<T> cachedValues;

    public void register(String key, T value) {
        if (writeLock) throw new IllegalStateException("write locked!");
        id2value.put(key, value);
        value2id.put(value, key);
        cachedValues = null;
    }

    public @Nullable String getId(T value) {
        return value2id.get(value);
    }

    public @Nullable T getByName(String name) {
        return id2value.get(name);
    }

    public void writeLock() {
        writeLock = true;
    }

    public T first() {
        if (first == null) {
            List<T> v = valuesList();
            first = v.isEmpty() ? null : v.getFirst();
        }
        return first;
    }

    private List<T> valuesList() {
        List<T> v = cachedValues;
        if (v == null) cachedValues = v = List.copyOf(id2value.values());
        return v;
    }

    public CyclicListIterator<T> cycle() {
        return new CyclicListIterator<>(valuesList());
    }

    public Collection<T> values() {
        return valuesList();
    }

    @Override
    public @NonNull Iterator<T> iterator() {
        return valuesList().iterator();
    }

    public int size() {
        return id2value.size();
    }

    public void reset() {
        writeLock = false;
        id2value.clear();
        value2id.clear();
        first = null;
        cachedValues = null;
    }
}
