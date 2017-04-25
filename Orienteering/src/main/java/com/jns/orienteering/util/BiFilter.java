package com.jns.orienteering.util;

@FunctionalInterface
public interface BiFilter<T, U> {

    public boolean test(T t, U u);
}
