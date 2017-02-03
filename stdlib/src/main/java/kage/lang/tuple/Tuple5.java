package kage.lang.tuple;

public class Tuple5<A, B, C, D, E> {
    private final A _1;
    private final B _2;
    private final C _3;
    private final D _4;
    private final E _5;

    public Tuple5(A _1, B _2, C _3, D _4, E _5) {
        this._1 = _1;
        this._2 = _2;
        this._3 = _3;
        this._4 = _4;
        this._5 = _5;
    }

    public A get_1() {
        return _1;
    }

    public B get_2() {
        return _2;
    }

    public C get_3() {
        return _3;
    }

    public D get_4() {
        return _4;
    }

    public E get_5() {
        return _5;
    }

    @Override
    public String toString() {
        return "(" + this._1 + ", " + this._2 + ", " + this._3 + ", " + this._4 + ", " + this._5 + ")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tuple5<?, ?, ?, ?, ?> tuple5 = (Tuple5<?, ?, ?, ?, ?>) o;

        if (!_1.equals(tuple5._1)) return false;
        if (!_2.equals(tuple5._2)) return false;
        if (!_3.equals(tuple5._3)) return false;
        if (!_4.equals(tuple5._4)) return false;
        return _5.equals(tuple5._5);
    }

    @Override
    public int hashCode() {
        int result = _1.hashCode();
        result = 31 * result + _2.hashCode();
        result = 31 * result + _3.hashCode();
        result = 31 * result + _4.hashCode();
        result = 31 * result + _5.hashCode();
        return result;
    }
}
