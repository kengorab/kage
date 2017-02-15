package kage.lang.util;

public class Primitives {
    public static int unbox(Integer i) { return i.intValue(); }
    public static double unbox(Double d) { return d.doubleValue(); }
    public static boolean unbox(Boolean b) { return b.booleanValue(); }
}
