/*
 * Created 25-Jun-2012
 * 
 * Copyright Samuel Halliday 2012
 * PROPRIETARY/CONFIDENTIAL. Use is subject to licence terms.
 */
package com.github.fommil.utils;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Iterables.filter;
import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Sets.intersection;
import static com.google.common.collect.Sets.newHashSet;

/**
 * Arbitrary convenience methods for working with a variety of standard Java
 * types.
 *
 * @author Samuel Halliday
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class Convenience {

    /**
     * @param <T>
     * @see #upperOuter(Iterable, com.github.fommil.utils.Convenience.Loop)
     */
    public interface Loop<T> {

        /**
         * @param first
         * @param second
         */
        public void action(T first, T second);
    }

    /**
     * Loop over the upper entries of the outer (matrix) product of the {@link Iterable}
     * and itself. Does not include diagonal entries and uses an ordering that
     * exhausts the second index before incrementing the first.
     * 
     * @param <T>
     * @param iterable
     * @param operation
     */
    public static <T> void upperOuter(Iterable<T> iterable, Loop<T> operation) {
        Preconditions.checkNotNull(iterable);
        Preconditions.checkNotNull(operation);
        List<T> list;
        if (iterable instanceof List) {
            list = (List<T>) iterable;
        } else {
            list = Lists.newArrayList(iterable);
        }
        for (int i = 0; i < list.size(); i++) {
            T first = list.get(i);
            for (int j = i + 1; j < list.size(); j++) {
                T second = list.get(j);
                operation.action(first, second);
            }
        }
    }

    /**
     * Useful for ensuring that there are no empty collections.
     */
    public static final Predicate<Collection<?>> NO_EMPTIES = new Predicate<Collection<?>>() {
        @Override
        public boolean apply(Collection<?> input) {
            if (input == null || input.isEmpty()) {
                return false;
            }
            return true;
        }
    };

    /**
     * Given a collection of sets, return the set of distinct sets:
     * i.e. merge all sets which share a common entry.
     *
     * @param <T>
     * @param sets
     * @see <a href="http://stackoverflow.com/questions/10634408/">Related discussion on Stack Overflow</a>
     * @return
     */
    public static <T> Set<Set<T>> disjointify(Collection<Set<T>> sets) {
        List<Set<T>> disjoint = newArrayList(sets);
        for (Set<T> set1 : disjoint) {
            for (Set<T> set2 : filter(disjoint, not(equalTo(set1)))) {
                if (!intersection(set1, set2).isEmpty()) {
                    // this wouldn't be safe for a Set<Set<T>>
                    set1.addAll(set2);
                    set2.clear();
                }
            }
        }
        return newHashSet(filter(disjoint, NO_EMPTIES));
    }

    /**
     * @param test
     * @param set
     * @return true if test is a subset of set
     */
    public static <T> boolean isSubset(Set<T> test, Set<T> set) {
        Preconditions.checkNotNull(test);
        Preconditions.checkNotNull(set);
        if (test.isEmpty()) {
            return false;
        }
        return Sets.intersection(test, set).size() == test.size();
    }
}
