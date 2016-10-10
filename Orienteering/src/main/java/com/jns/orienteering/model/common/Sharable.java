package com.jns.orienteering.model.common;

public interface Sharable extends Model {

    String getOwnerId();

    AccessType getAccessType();

    boolean accessTypeChanged();


}
