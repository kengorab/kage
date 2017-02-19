package kage.lang.collection;

import java.util.Arrays;

import kage.lang.util.Maybe;

public class Array<T> implements Indexable<Integer, T> {
    public T[] items;
    private int size;

    public Array(T[] items) {
        this.items = items;
        this.size = items.length;
    }

    public int getSize() {
        return size;
    }

    public Maybe<T> getAt(Integer index) {
        if (index >=0 && index < this.items.length)
            return new Maybe.Some<T>(this.items[index]);
        else
            return new Maybe.None<T>();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < items.length; i++) {
            sb.append(items[i].toString());
            if (i != items.length - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Array<?> array = (Array<?>) o;

        if (size != array.size) return false;
        // Probably incorrect - comparing Object[] arrays with Arrays.equals
        return Arrays.equals(items, array.items);
    }

    @Override
    public int hashCode() {
        int result = Arrays.hashCode(items);
        result = 31 * result + size;
        return result;
    }
}
