package org.usbtechno.deve;

public class TraceContext {

    private static final ThreadLocal<Trace> CURRENT = new ThreadLocal<>();

    public static void set(Trace trace) {
        CURRENT.set(trace);
    }

    public static Trace get() {
        return CURRENT.get();
    }

    public static void clear() {
        CURRENT.remove();
    }
}