package net.mika.mikamods.event;

import java.util.*;

public class EventBus {
    private static final Map<Class<?>, List<EventListener<?>>> listeners = new HashMap<>();

    public static <T extends Event> void register(Class<T> type, EventListener<T> listener) {
        listeners.computeIfAbsent(type, k -> new ArrayList<>()).add(listener);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Event> void post(T event) {
        List<EventListener<?>> list = listeners.get(event.getClass());

        if (list == null) return;

        for (EventListener<?> listener : list) {
            ((EventListener<T>) listener).handle(event);
        }
    }
}
