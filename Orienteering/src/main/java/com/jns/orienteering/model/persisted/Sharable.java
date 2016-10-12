package com.jns.orienteering.model.persisted;

public interface Sharable extends Model {

    String getOwnerId();

    AccessType getAccessType();

    boolean accessTypeChanged();


}
