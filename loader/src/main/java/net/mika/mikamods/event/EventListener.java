package net.mika.mikamods.event;

public interface EventListener<T extends Event> {
    void handle(T event);
}