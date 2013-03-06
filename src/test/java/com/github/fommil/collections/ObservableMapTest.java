// Copyright (c) 2013 Samuel Halliday
package com.github.fommil.collections;

import com.github.fommil.collections.ObservableMap.Change;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;

/**
 *
 * @author Samuel Halliday
 */
public class ObservableMapTest {

    private ObservableMap<String, String> newTestMap() {
        Map<String, String> map = Maps.newHashMap();
        map.put("1", "A");
        map.put("2", "B");
        map.put("3", "C");
        map.put("4", "D");
        return ObservableMap.newObservableMap(map);
    }

    @Test
    public void testPut() {
        ObservableMap<String, String> map = newTestMap();
        final AtomicBoolean listened = new AtomicBoolean();
        map.addMapListener(new ObservableMap.MapListener<String, String>() {
            @Override
            public void onMapChanged(Change<String, String> change) {
                assertTrue(change.wasRemoved());
                assertTrue(change.wasAdded());
                assertEquals(1, change.getEntriesRemoved().size());
                Entry<String, String> removed = Iterables.getOnlyElement(change.getEntriesRemoved());
                assertEquals(1, change.getEntriesAdded().size());
                Entry<String, String> added = Iterables.getOnlyElement(change.getEntriesAdded());
                assertEquals("4", removed.getKey());
                assertEquals("4", added.getKey());
                assertEquals("D", removed.getValue());
                assertEquals("DD", added.getValue());
                listened.set(true);
            }
        });
        map.put("4", "DD");
        assertEquals(4, map.size());
        assertTrue(listened.get());
    }

    @Test
    public void testRemove() {
        ObservableMap<String, String> map = newTestMap();
        final AtomicBoolean listened = new AtomicBoolean();
        map.addMapListener(new ObservableMap.MapListener<String, String>() {
            @Override
            public void onMapChanged(Change<String, String> change) {
                assertTrue(change.wasRemoved());
                assertFalse(change.wasAdded());
                assertEquals(0, change.getEntriesAdded().size());
                assertEquals(1, change.getEntriesRemoved().size());
                Entry<String, String> removed = Iterables.getOnlyElement(change.getEntriesRemoved());
                assertEquals("4", removed.getKey());
                assertEquals("D", removed.getValue()); // second removal doesn't fire event
                listened.set(true);
            }
        });
        map.remove("4");
        map.remove("4");
        assertEquals(3, map.size());
        assertTrue(listened.get());
    }

    @Test
    public void testPutAll() {
        ObservableMap<String, String> map = newTestMap();
        final AtomicBoolean listened = new AtomicBoolean();
        map.addMapListener(new ObservableMap.MapListener<String, String>() {
            @Override
            public void onMapChanged(Change<String, String> change) {
                assertTrue(change.wasRemoved());
                assertTrue(change.wasAdded());
                assertEquals(4, change.getEntriesRemoved().size());
                assertEquals(4, change.getEntriesAdded().size());
                listened.set(true);
            }
        });
        ObservableMap<String, String> putter = newTestMap();
        map.putAll(putter);
        assertEquals(4, map.size());
        assertTrue(listened.get());
    }

    @Test
    public void testClear() {
        ObservableMap<String, String> map = newTestMap();
        final AtomicBoolean listened = new AtomicBoolean();
        map.addMapListener(new ObservableMap.MapListener<String, String>() {
            @Override
            public void onMapChanged(Change<String, String> change) {
                assertTrue(change.wasRemoved());
                assertFalse(change.wasAdded());
                assertEquals(4, change.getEntriesRemoved().size());
                listened.set(true);
            }
        });
        map.clear();
        assertEquals(0, map.size());
        assertTrue(listened.get());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testKeySetAdd() {
        ObservableMap<String, String> map = newTestMap();
        map.keySet().add("FAIL");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testKeySetAddAll() {
        ObservableMap<String, String> map = newTestMap();
        map.keySet().addAll(Lists.newArrayList("FAIL", "EPICALLY"));
    }

    public void testKeySetClear() {
        ObservableMap<String, String> map = newTestMap();
        final AtomicBoolean listened = new AtomicBoolean();
        map.addMapListener(new ObservableMap.MapListener<String, String>() {
            @Override
            public void onMapChanged(Change<String, String> change) {
                assertTrue(change.wasRemoved());
                assertFalse(change.wasAdded());
                assertEquals(4, change.getEntriesRemoved().size());
                listened.set(true);
            }
        });
        map.keySet().clear();
        assertEquals(0, map.size());
        assertTrue(listened.get());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testValuesAdd() {
        newTestMap().values().add("A");
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testValuesRemove() {
        newTestMap().values().remove("A");
    }

    @Test
    public void testEntrySetRetainAll() {
        ObservableMap<String, String> map = newTestMap();
        final AtomicBoolean listened = new AtomicBoolean();
        map.addMapListener(new ObservableMap.MapListener<String, String>() {
            @Override
            public void onMapChanged(Change<String, String> change) {
                assertTrue(change.wasRemoved());
                assertFalse(change.wasAdded());
                assertEquals(3, change.getEntriesRemoved().size());
                listened.set(true);
            }
        });
        Entry<String, String> keeper = new SimpleEntry<String, String>("1", "A");
        map.entrySet().retainAll(Collections.singleton(keeper));
        assertEquals(1, map.size());
        assertTrue(listened.get());
    }
}
