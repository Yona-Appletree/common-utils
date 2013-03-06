/*
 * Created 03-Aug-2012
 * 
 * Copyright Samuel Halliday 2012
 * PROPRIETARY/CONFIDENTIAL. Use is subject to licence terms.
 */
package com.github.fommil.collections;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeSupport;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.concurrent.NotThreadSafe;
import lombok.Delegate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ListenerSupport;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

/**
 * Wrapper that allows changes to a {@link Collection} to be observed. This is
 * an internally written class because Guava opted not to implement this
 * functionality.
 * <p>
 * Not able to detect changes to mutable elements. May not send events if
 * an operation was attempted which did not change the underlying
 * {@link Collection}.
 * <p>
 * Actions on the underlying {@link Collection} will obviously no longer be
 * atomic, which may affect the thread safety of their use.
 * This will otherwise not affect the thread safety of the delegate, but
 * makes no guarantees about the thread safety of the observations,
 * i.e. observations may arrive out of
 * sync with the actual order in which they were performed and may already be
 * out of date by the time they are received.
 * <p>
 * Listeners should be mindful of what they intend to do with observations, as
 * changes to the underlying {@link Collection} may result in unexpected
 * behaviour.
 * <p>
 * Note that in order to allow the generics to work for {@code remove*} operations
 * in the {@link CollectionListener} interface, all elements will be cast into the
 * {@link Collection}'s generic type - which may not be compatible with (very badly
 * written!) legacy collections.
 * <p>
 * For a very functional {@code ObservableList} look at GlazedLists (or perhaps
 * it might be worthwhile implementing the extra functionality here).
 * <p>
 * Equality and hash codes ignore the registered listeners, calculating purely
 * based on content, as users have come to expect of the Collections API.
 * 
 * @param <T> 
 * @author Samuel Halliday
 * @see ObservableMap
 * @see ObservableSet
 * @see <a href="http://code.google.com/p/guava-libraries/issues/detail?id=1077">Guava RFE</a>
 * @see <a href="http://docs.oracle.com/javafx/2/api/javafx/collections/package-frame.html">JavaFX Collections</a>
 */
@RequiredArgsConstructor
@NotThreadSafe
@ListenerSupport(ObservableCollection.CollectionListener.class)
@EqualsAndHashCode(of = "delegate")
public class ObservableCollection<T> implements Collection<T> {

    /**
     * @param <T>
     * @param collection
     * @return 
     */
    public static <T> ObservableCollection<T> newObservableCollection(Collection<T> collection) {
        Preconditions.checkNotNull(collection);
        return new ObservableCollection<T>(collection);
    }

    @RequiredArgsConstructor
    @Getter
    public static final class Change<T> {

        @NonNull
        private final ObservableCollection<T> collection;

        @NonNull
        private final Collection<T> elementsAdded, elementsRemoved;

        @Accessors(fluent = true)
        private final boolean wasAdded, wasRemoved;

    }

    /**
     * Registers a {@link CollectionListener} with the {@link ObservableCollection} which
     * calls {@link PropertyChangeSupport#firePropertyChange(PropertyChangeEvent)}
     * every time a change is detected. For efficiency of implementation, the
     * {@code oldValue} is always {@code null}.
     * 
     * @param <T>
     * @param pcs
     * @param collection
     * @param name
     */
    public static <T> void propertyChangeAdapter(
            final PropertyChangeSupport pcs,
            final ObservableCollection<T> collection,
            final String name) {
        Preconditions.checkNotNull(pcs);
        Preconditions.checkNotNull(collection);
        Preconditions.checkNotNull(name);
        CollectionListener<T> listener = new CollectionListener<T>() {
            @Override
            public void onCollectionChanged(Change<T> change) {
                pcs.firePropertyChange(name, null, collection);
            }
        };
        collection.addCollectionListener(listener);
    }

    /**
     * Listen to changes in {@link ObservableCollection}s.
     *
     * @param <T> 
     */
    public interface CollectionListener<T> {

        /**
         * Called after a change has been made to an {@link ObservableCollection}.
         * Note that in bulk updates, all elements which were requested to be
         * added (or removed) will be included, but this does not guarantee that the
         * elements were present (or not present) before the operation.
         * 
         * @param change
         */
        public void onCollectionChanged(Change<T> change);
    }

    // subset of the Collection API which results in changes
    private interface Mutators<T> {

        public Iterator<T> iterator();

        public boolean add(T e);

        public boolean remove(Object o);

        public boolean addAll(Collection<? extends T> c);

        public boolean removeAll(Collection<?> c);

        public boolean retainAll(Collection<?> c);

        public void clear();
    }

    @Delegate(excludes = Mutators.class)
    protected final Collection<T> delegate;

    private Change<T> createAdditionChange(T added) {
        return createAdditionChange(Collections.singleton(added));
    }

    private Change<T> createAdditionChange(Collection<T> added) {
        return new Change<T>(this, added, Collections.<T>emptySet(), true, false);
    }

    private Change<T> createRemovalChange(T removed) {
        return createRemovalChange(Collections.singleton(removed));
    }

    private Change<T> createRemovalChange(Collection<T> removed) {
        return new Change<T>(this, Collections.<T>emptySet(), removed, false, true);
    }

    // HERE DOWN IS THE IMPLEMENTATION
    @Override
    public Iterator<T> iterator() {
        final Iterator<T> iterator = delegate.iterator();
        return new Iterator<T>() {
            private volatile T current;

            @Override
            public boolean hasNext() {
                return iterator.hasNext();
            }

            @Override
            public T next() {
                current = iterator.next();
                return current;
            }

            @Override
            public void remove() {
                iterator.remove();
                fireOnCollectionChanged(createRemovalChange(current));
            }
        };
    }

    @Override
    public boolean add(T e) {
        if (delegate.add(e)) {
            fireOnCollectionChanged(createAdditionChange(e));
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        if (delegate.remove(o)) {
            fireOnCollectionChanged(createRemovalChange((T) o));
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean addAll(Collection<? extends T> c) {
        if (delegate.addAll(c)) {
            fireOnCollectionChanged(createAdditionChange((Collection<T>) c));
            return true;
        }
        return false;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean removeAll(Collection<?> c) {
        if (delegate.removeAll(c)) {
            fireOnCollectionChanged(createRemovalChange((Collection<T>) c));
            return true;
        }
        return false;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        List<T> before = Lists.newArrayList(delegate);
        if (delegate.retainAll(c)) {
            List<T> lost = Lists.newArrayList();
            for (T old : before) {
                if (!c.contains(old)) {
                    lost.add(old);
                }
            }
            fireOnCollectionChanged(createRemovalChange(lost));
            return true;
        }
        return false;
    }

    @Override
    public void clear() {
        List<T> before = Lists.newArrayList(delegate);
        delegate.clear();
        if (!before.isEmpty()) {
            fireOnCollectionChanged(createRemovalChange(before));
        }
    }

    @Override
    public String toString() {
        return delegate.toString();
    }
}
