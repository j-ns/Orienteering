package com.jns.orienteering.control;

import java.util.Optional;

import com.gluonhq.charm.down.Services;
import com.gluonhq.charm.down.plugins.KeyboardService;

import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;

public class RefocusableTextField extends TextField {

    private Optional<KeyboardService> service;

    public RefocusableTextField(String text) {
        this();
        setText(text);
    }

    public RefocusableTextField() {
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