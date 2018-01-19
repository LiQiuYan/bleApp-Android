package com.healthme.common.util;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class LimitedCircularQueue<T> {

    private final int limit;
    private final List<T> list = Collections.synchronizedList(new LinkedList<T>());

    public LimitedCircularQueue(int limit) {
        this.limit = limit;
    }

    private boolean trim() {
        boolean changed = list.size() > limit;
        while (list.size() > limit) {
            list.remove(0);
        }
        return changed;
    }

    public boolean add(T o) {
        boolean changed = list.add(o);
        boolean trimmed = trim();
        return changed || trimmed;
    }

    public int size() {
        return list.size();
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }

    public Iterator<T> iterator() {
        return list.iterator();
    }

    public Object[] toArray() {
        return list.toArray();
    }

    public <T> T[] toArray(T[] a) {
        return list.toArray(a);
    }

    public boolean remove(Object o) {
        return list.remove(o);
    }

    public boolean containsAll(Collection<?> c) {
        return list.containsAll(c);
    }

    public boolean addAll(Collection<? extends T> c) {
        boolean changed = list.addAll(c);
        boolean trimmed = trim();
        return changed || trimmed;
    }

    public boolean removeAll(Collection<?> c) {
        return list.removeAll(c);
    }

    public boolean retainAll(Collection<?> c) {
        return list.retainAll(c);
    }

    public void clear() {
        list.clear();
    }
}