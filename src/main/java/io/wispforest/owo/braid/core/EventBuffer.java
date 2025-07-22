package io.wispforest.owo.braid.core;

import io.wispforest.owo.braid.core.events.UserEvent;

import java.util.ArrayList;
import java.util.List;

public class EventBuffer {

    private final List<UserEvent> bufferedEvents = new ArrayList<>();

    public void add(UserEvent event) {
        this.bufferedEvents.add(event);
    }

    public List<UserEvent> poll() {
        var events = new ArrayList<>(this.bufferedEvents);
        this.bufferedEvents.clear();

        return events;
    }
}
