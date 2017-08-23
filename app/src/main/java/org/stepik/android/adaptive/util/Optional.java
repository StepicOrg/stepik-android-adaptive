package org.stepik.android.adaptive.util;

/**
 * Wrapper for null objects in RxJava
 * @param <T>
 */
public final class Optional<T> {
    public final T value;
    public Optional(T value) {
        this.value = value;
    }

    public boolean isNotEmpty() {
        return value != null;
    }
}