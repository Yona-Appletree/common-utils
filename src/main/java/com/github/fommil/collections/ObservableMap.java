// Copyright (c) 2013 Samuel Halliday
package com.github.fommil.collections;

import com.github.fommil.collections.ObservableCollection.CollectionListener;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.*;
import lombok.experimental.Accessors;

import javax.annotation.concurrent.NotThreadSafe;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.AbstractMap.SimpleEntry;
import java.util.*;

/**
 * Compliments {@link ObservableCollection} for a {@link Map}.
 * <p>
 * Equality and hash codes ignore the registered listeners, calculating purely
 * based on content, as users have come to expect of the Collections API.
 * 
 * @param <K> 
 * @param <V> 
 * @author Samuel Halliday
 * @see ObservableCollection
 */
@RequiredArgsConstructor
@NotThreadSafe
@ListenerSupport(ObservableMap.MapListener.class)
@EqualsAndHashCode(of = "delegate")
public class ObservableMap<K, V> implements Map<K, V> {

    /**
     * @param <K> 
     * @param <V> 
     * @param map
     * @return 
     */
    public static <K, V> ObservableMap<K, V> newObservableMap(Map<K, V> map) {
        return new ObservableMap<K, V>(map);
    }

    /**
     * @param <K>
     * @param <V>
     * @return 
     */
    public static <K, V> ObservableMap<K, V> newObservableHashMap() {
        return newObservableMap(Maps.<K, V>newHashMap());
    }

    /**
     * @param <K>
     * @param <V>
     * @return 
     */
    public static <K extends Comparable<K>, V> ObservableMap<K, V> newObservableTreeMap() {
        return newObservableMap(Maps.<K, V>newTreeMap());
    }

    /**
     * Registers a {@link MapListener} with the {@link ObservableMap} which
     * calls {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * every time a change is detected. For efficiency of implementation, the
     * {@code oldValue} is always {@code null}.
     * 
     * @param <K>
     * @param <V>
     * @param pcs
     * @param map
     * @param name
     */
    public static <K, V> void propertyChangeAdapter(
            final PropertyChangeSupport pcs,
            final ObservableMap<K, V> map,
            final String name) {
        Preconditions.checkNotNull(pcs);
        Preconditions.checkNotNull(map);
        Preconditions.checkNotNull(name);
        MapListener<K, V> listener = new MapListener<K, V>() {
            @Override
            public void onMapChanged(Change<K, V> change) {
                pcs.firePropertyChange(name, null, map);
            }
        };
        map.addMapListener(listener);
    }

    @RequiredArgsConstructor
    @Getter
    public static final class Change<K, V> {

        @NonNull
        private final ObservableMap<K, V> map;

        @NonNull
        private final Collection<Entry<K, V>> entriesAdded, entriesRemoved;

        @Accessors(fluent = true)
        private final boolean wasAdded, wasRemoved;

    }

    /**
     * Listen to changes in {@link ObservableCollection}s.
     *
     * @param <K> 
     * @param <V> 
     */
    public interface MapListener<K, V> {

        /**
         * Called after a change has been attempted to an {@link ObservableMap}.
         * Note that in bulk updates, all elements which were requested to be
         * added (or removed) will be included, but this does not guarantee that the
         * elements were present (or not present) before the operation.
         * <p>
         * In addition, operations may include the list of removed and added
         * entries (e.g. if a value is replaced) but this does not imply
         * that the replaced entries will always be included.
         * 
         * @param change
         */
        public void onMapChanged(Change<K, V> change);
    }

    // subset of the Collection API which results in changes
    private interface Mutators<K, V> {

        public V put(K key, V value);

        public V remove(Object key);

        public void putAll(Map<? extends K, ? extends V> m);

        public void clear();

        public Set<K> keySet();

        public Collection<V> values();

        public Set<Entry<K, V>> entrySet();
    }

    @Delegate(excludes = Mutators.class)
    private final Map<K, V> delegate;

    private Change<K, V> createAdditionChange(Entry<K, V> added) {
        return createAdditionChange(Collections.singleton(added));
    }

    private Change<K, V> createAdditionChange(Collection<Entry<K, V>> added) {
        return new Change<K, V>(this, added, Collections.<Entry<K, V>>emptySet(), true, false);
    }

    private Change<K, V> createRemovalChange(Entry<K, V> removed) {
        return createRemovalChange(Collections.singleton(removed));
    }

    private Change<K, V> createRemovalChange(Collection<Entry<K, V>> removed) {
        return new Change<K, V>(this, Collections.<Entry<K, V>>emptySet(), removed, false, true);
    }

    private Change<K, V> createUpdateChange(Entry<K, V> removed, Entry<K, V> added) {
        return createUpdateChange(Collections.singleton(removed), Collections.singleton(added));
    }

    private Change<K, V> createUpdateChange(Collection<Entry<K, V>> removed, Collection<Entry<K, V>> added) {
        return new Change<K, V>(this, added, removed, true, true);
    }

    @Override
    public V put(K key, V value) {
        V old = delegate.put(key, value);
        Change<K, V> change;
        if (old == null) {
            change = createAdditionChange(new SimpleEntry<K, V>(key, value));
        } else {
            Entry<K, V> oldEntry = new SimpleEntry<K, V>(key, old);
            Entry<K, V> newEntry = new SimpleEntry<K, V>(key, value);
            change = createUpdateChange(oldEntry, newEntry);
        }
        fireOnMapChanged(change);
        return old;
    }

    @Override
    @SuppressWarnings("unchecked")
    public V remove(Object key) {
        if (delegate.containsKey((K) key)) {
            V value = delegate.remove(key);
            fireOnMapChanged(createRemovalChange(new SimpleEntry<K, V>((K) key, value)));
            return value;
        }
        return null;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void putAll(Map<? extends K, ? extends V> m) {
        Map<K, V> oldEntries = Maps.newHashMap();
        for (K key : m.keySet()) {
            if (delegate.containsKey(key)) {
                oldEntries.put(key, delegate.get(key));
            }
        }
        delegate.putAll(m);
        Set newEntries = m.entrySet(); // erasing generics
        fireOnMapChanged(createUpdateChange(oldEntries.entrySet(), newEntries));
    }

    @Override
    public void clear() {
        Set<Entry<K, V>> all = Sets.newHashSet(delegate.entrySet());
        delegate.clear();
        fireOnMapChanged(createRemovalChange(all));
    }

    @Override
    public Set<K> keySet() {
        Set<K> keySet = delegate.keySet();
        ObservableSet<K> observable = ObservableSet.newObservableSet(keySet);
        final Map<K, V> before = Maps.newHashMap(delegate); // inefficient
        observable.addCollectionListener(new CollectionListener<K>() {
            @Override
            public void onCollectionChanged(ObservableCollection.Change<K> change) {
                if (change.wasAdded()) {
                    throw new UnsupportedOperationException("Map.keySet() is not supposed to allow add*");
                }
                List<Entry<K, V>> entries = Lists.newArrayList();
                for (K key : change.getElementsRemoved()) {
                    V value = before.get(key);
                    entries.add(new SimpleEntry<K, V>(key, value));
                }
                fireOnMapChanged(createRemovalChange(entries));
            }
        });
        return observable;
    }

    @Override
    public Collection<V> values() {
        Collection<V> values = delegate.values();
        ObservableCollection<V> observable = ObservableCollection.newObservableCollection(values);
        observable.addCollectionListener(new CollectionListener<V>() {
            @Override
            public void onCollectionChanged(ObservableCollection.Change<V> change) {
                throw new UnsupportedOperationException("Changes to Map.values() are not supported by ObservableMap");
            }
        });
        return observable;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        Set<Entry<K, V>> entrySet = delegate.entrySet();
        ObservableSet<Entry<K, V>> observable = ObservableSet.newObservableSet(entrySet);
        observable.addCollectionListener(new CollectionListener<Entry<K, V>>() {
            @Override
            public void onCollectionChanged(ObservableCollection.Change<Entry<K, V>> change) {
                if (change.wasAdded()) {
                    throw new UnsupportedOperationException("Map.entrySet() is not supposed to allow add*");
                }
                fireOnMapChanged(createRemovalChange(change.getElementsRemoved()));
            }
        });
        return observable;
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
