package com.jns.orienteering.model.persisted;

import javax.xml.bind.annotation.XmlElement;

public class BasePostableSynchronizable extends BaseSynchronizable implements Postable {

    private String               postId;

    @Override
    @XmlElement(name = "name")
    public String getPostId() {
        return postId;
    }

    @Override
    public void setPostId(String name) {
        postId = name;
    }

}
