package com.poc.model;

import java.util.ArrayList;
import java.util.List;

public class EventEmitter {

    private final List<EventListener> listeners = new ArrayList<>();


    public void subscribe(EventListener listener) {
        listeners.add(listener);
    }


    public void emit(String eventData) {
        for (EventListener listener : listeners) {
            listener.onEvent(eventData);
        }
    }
}
