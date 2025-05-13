package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.feed.Event;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventStorage eventStorage;

    public List<Event> getEventsByUserId(int userId) {
        return eventStorage.getEventsByUserId(userId);
    }

    public void addEvent(Event event) {  // Изменили возвращаемый тип на void
        eventStorage.addEvent(event);
    }
}