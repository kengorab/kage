package kage.lang.collection;

import kage.lang.util.Maybe;

public interface Indexable<I, V> {
    Maybe<V> getAt(I index);
}
