package com.crschnick.pdx_unlimiter.core.util;

import java.util.AbstractList;
import java.util.List;

public final class JoinedList<E> extends AbstractList<E> {

    private final List<List<E>> lists;

    public JoinedList(List<List<E>> lists) {
        this.lists = lists;
    }

    @Override
    public E get(final int index) {
        return getList(index).get(getListIndex(index));
    }

    @Override
    public E set(final int index, final E element) {
        return getList(index).set(getListIndex(index), element);
    }

    @Override
    public void add(final int index, final E element) {
        getList(index).add(getListIndex(index), element);
    }

    @Override
    public E remove(final int index) {
        return getList(index).remove(getListIndex(index));
    }

    @Override
    public int size() {
        return lists.stream().mapToInt(List::size).sum();
    }

    @Override
    public void clear() {
        lists.forEach(List::clear);
    }

    private int getListIndex(int index) {
        for (var l : lists) {
            if (index > l.size()) {
                index -= l.size();
            }
        }
        return index;
    }

    private List<E> getList(final int index) {
        int count = 0;
        for (var l : lists) {
            if (index <= count + l.size()) {
                return l;
            }
        }
        throw new IllegalArgumentException();
    }
}