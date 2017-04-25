package com.jns.orienteering.control;

import java.util.Optional;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.KeyboardService;

import javafx.scene.control.TextArea;
import javafx.scene.input.MouseEvent;

public class RefocusableTextArea extends TextArea {

    private Optional<KeyboardService> service;

    public RefocusableTextArea(String text) {
        this();
        setText(text);

    }

    public RefocusableTextArea() {
        service = Services.get(KeyboardService.class);

        addEventFilter(MouseEvent.MOUSE_PRESSED, event ->
        {
            if (!isFocused()) {
                event.consume();
            }
        });

        addEventHandler(MouseEvent.MOUSE_CLICKED, e ->
        {
            if (!isFocused()) {
                requestFocus();
                end();

            } else {
                service.ifPresent(KeyboardService::show);
            }
        });
    }

}
