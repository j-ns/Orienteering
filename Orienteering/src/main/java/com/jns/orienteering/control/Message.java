package com.jns.orienteering.control;

public class Message {

    private String title;
    private String text;

    private Message() {
    }

    private Message(String title, String text) {
        this.title = title;
        this.text = text;
    }

    public static Message create() {
        return new Message();
    }

    public static Message titleAndText(String title, String text) {
        return new Message(title, text);
    }

    public Message title(String title) {
        this.title = title;
        return this;
    }

    public Message text(String text) {
        this.text = text;
        return this;
    }

    public String getTitle() {
        return title;
    }

    public String getText() {
        return text;
    }

}
