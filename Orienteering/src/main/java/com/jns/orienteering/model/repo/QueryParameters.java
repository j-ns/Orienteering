package com.jns.orienteering.model.repo;

import javafx.util.Pair;

public class QueryParameters {

    public static Pair<String, String> startAt(long timeStamp) {
        return newPair("startAt", Long.toString(timeStamp));
    }

    public static Pair<String, String> endAt(long timeStamp) {
        return newPair("endAt", Long.toString(timeStamp));
    }

    public static Pair<String, String> shallow() {
        return newPair("shallow", "true");
    }

    public static Pair<String, String> newPair(String key, String value) {
        return new Pair<String, String>(key, value);
    }

}
