package com.jns.orienteering.model.repo;

import javafx.util.Pair;

@SuppressWarnings("serial")
class QueryParameter extends Pair<String, String> {

    private static final QueryParameter SHALLOW             = new QueryParameter("shallow", "true");
    private static final QueryParameter DELETE_OVERRIDE     = new QueryParameter("x-http-method-override", "Delete");
    private static final QueryParameter ORDER_BY_TIME_STAMP = orderBy("timeStamp");

    QueryParameter(String key, String value) {
        super(key, value);
    }

    static QueryParameter shallow() {
        return SHALLOW;
    }

    static QueryParameter deleteOverride() {
        return DELETE_OVERRIDE;
    }

    static QueryParameter orderByTimeStamp() {
        return ORDER_BY_TIME_STAMP;
    }

    static QueryParameter orderBy(String orderBy) {
        return new QueryParameter("orderBy", "\"" + orderBy + "\"");
    }

    static QueryParameter startAt(long timeStamp) {
        return new QueryParameter("startAt", Long.toString(timeStamp));
    }

    static QueryParameter endAt(long timeStamp) {
        return new QueryParameter("endAt", Long.toString(timeStamp));
    }

}
