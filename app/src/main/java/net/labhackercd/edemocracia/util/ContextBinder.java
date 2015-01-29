package net.labhackercd.edemocracia.util;

import android.content.Context;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * Small utility to bind anything to Contexts. It's based on WeakHashMaps.
 *
 * @param <T>
 */
public class ContextBinder<T> {
    Map<Context, T> binds = new WeakHashMap<>();

    public T get(Context context, T defaults) {
        if (binds.containsKey(context)) {
            defaults = binds.get(context);
        }
        return defaults;
    }

    public T put(Context context, T value) {
        binds.put(context, value);
        return value;
    }
}
