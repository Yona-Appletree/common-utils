/*
 * Created 03-Aug-2012
 * 
 * Copyright Samuel Halliday 2012
 * PROPRIETARY/CONFIDENTIAL. Use is subject to licence terms.
 */
package com.github.fommil.collections;

import com.github.fommil.collections.ObservableCollection;
import com.google.common.collect.Lists;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import static org.junit.Assert.*;
import com.github.fommil.collections.ObservableCollection.Change;
import com.github.fommil.collections.ObservableCollection.CollectionListener;

/**
 *
 * @author Samuel Halliday
 */
public class ObservableCollectionTest {
    
    private ObservableCollection<String> newTestCollection() {
        return ObservableCollection.newObservableCollection(Lists.newArrayList("A", "B", "C", "D", "E", "F"));
    }
    
    @Test
    public void testIterator() {
        ObservableCollection<String> collection = newTestCollection();
        
        final AtomicBoolean listened = new AtomicBoolean();
        collection.addCollectionListener(new CollectionListener<String>() {
            @Override
            public void onCollectionChanged(Change<String> change) {
                listened.set(true);
                assertNotNull(change.getCollection());
                
                assertFalse(change.wasAdded());
                assertTrue(change.wasRemoved());
                assertEquals(0, change.getElementsAdded().size());
                assertEquals(1, change.getElementsRemoved().size());
            }
        });
        Iterator<String> it = collection.iterator();
        while (it.hasNext()) {
            it.next();
            it.remove();
        }
        assertEquals(0, collection.size());
        assertTrue(listened.get());
    }
    
    @Test
    public void testAdd() {
        ObservableCollection<String> collection = newTestCollection();
        
        final AtomicBoolean listened = new AtomicBoolean();
        collection.addCollectionListener(new CollectionListener<String>() {
            @Override
            public void onCollectionChanged(Change<String> change) {
                listened.set(true);
                assertNotNull(change.getCollection());
                
                assertTrue(change.wasAdded());
                assertFalse(change.wasRemoved());
                assertEquals(1, change.getElementsAdded().size());
                assertEquals(Collections.singleton("G"), change.getElementsAdded());
                assertEquals(0, change.getElementsRemoved().size());
            }
        });
        collection.add("G");
        assertEquals(7, collection.size());
        assertTrue(listened.get());
    }
    
    @Test
    public void testAddAll() {
        ObservableCollection<String> collection = newTestCollection();
        
        final AtomicBoolean listened = new AtomicBoolean();
        collection.addCollectionListener(new CollectionListener<String>() {
            @Override
            public void onCollectionChanged(Change<String> change) {
                listened.set(true);
                assertNotNull(change.getCollection());
                
                assertTrue(change.wasAdded());
                assertFalse(change.wasRemoved());
                assertEquals(3, change.getElementsAdded().size());
                assertEquals(Lists.newArrayList("F", "G", "H"), change.getElementsAdded());
                assertEquals(0, change.getElementsRemoved().size());
            }
        });
        collection.addAll(Lists.newArrayList("F", "G", "H"));
        assertEquals(9, collection.size()); // List, not Set
        assertTrue(listened.get());
    }
    
    @Test
    public void testRemove() {
        ObservableCollection<String> collection = newTestCollection();
        
        final AtomicBoolean listened = new AtomicBoolean();
        collection.addCollectionListener(new CollectionListener<String>() {
            @Override
            public void onCollectionChanged(Change<String> change) {
                listened.set(true);
                assertNotNull(change.getCollection());
                
                assertFalse(change.wasAdded());
                assertTrue(change.wasRemoved());
                assertEquals(1, change.getElementsRemoved().size());
                assertEquals(Collections.singleton("A"), change.getElementsRemoved());
                assertEquals(0, change.getElementsAdded().size());
            }
        });
        collection.remove("A");
        assertEquals(5, collection.size());
        assertTrue(listened.get());
    }
    
    @Test
    public void testRemoveAll() {
        ObservableCollection<String> collection = newTestCollection();
        
        final AtomicBoolean listened = new AtomicBoolean();
        collection.addCollectionListener(new CollectionListener<String>() {
            @Override
            public void onCollectionChanged(Change<String> change) {
                listened.set(true);
                assertNotNull(change.getCollection());
                
                assertFalse(change.wasAdded());
                assertTrue(change.wasRemoved());
                assertEquals(3, change.getElementsRemoved().size());
                assertEquals(Lists.newArrayList("A", "C", "Z"), change.getElementsRemoved());
                assertEquals(0, change.getElementsAdded().size());
            }
        });
        collection.removeAll(Lists.newArrayList("A", "C", "Z"));
        assertEquals(4, collection.size());
        assertTrue(listened.get());
    }
    
    @Test
    public void testRetainAll() {
        ObservableCollection<String> collection = newTestCollection();
        
        final AtomicBoolean listened = new AtomicBoolean();
        collection.addCollectionListener(new CollectionListener<String>() {
            @Override
            public void onCollectionChanged(Change<String> change) {
                listened.set(true);
                assertNotNull(change.getCollection());
                
                assertFalse(change.wasAdded());
                assertTrue(change.wasRemoved());
                assertTrue(change.getElementsRemoved().size() > 0);
                assertEquals(0, change.getElementsAdded().size());
            }
        });
        collection.retainAll(Lists.newArrayList("A", "Z"));
        assertEquals(1, collection.size());
        assertTrue(listened.get());
    }
    
    @Test
    public void testClear() {
        ObservableCollection<String> collection = newTestCollection();
        
        final AtomicBoolean listened = new AtomicBoolean();
        collection.addCollectionListener(new CollectionListener<String>() {
            @Override
            public void onCollectionChanged(Change<String> change) {
                listened.set(true);
                assertNotNull(change.getCollection());
                
                assertFalse(change.wasAdded());
                assertTrue(change.wasRemoved());
                assertTrue(change.getElementsRemoved().size() > 0);
                assertEquals(0, change.getElementsAdded().size());
            }
        });
        collection.clear();
        assertEquals(0, collection.size());
        assertTrue(listened.get());
    }
}
