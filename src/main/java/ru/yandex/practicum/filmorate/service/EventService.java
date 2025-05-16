package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.feed.Event;
import ru.yandex.practicum.filmorate.model.feed.EventOperation;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.storage.event.EventStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {
    private final EventStorage eventStorage;
    private final UserStorage userStorage;

    public List<Event> getEventsByUserId(int userId) {
        userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        return eventStorage.getEventsByUserId(userId);
    }

    public void addEvent(Event event) {
        eventStorage.addEvent(event);
    }

    public void addLikeEvent(int userId, int filmId) {
        addEvent(createEvent(userId, filmId, EventType.LIKE, EventOperation.ADD));
    }

    public void removeLikeEvent(int userId, int filmId) {
        addEvent(createEvent(userId, filmId, EventType.LIKE, EventOperation.REMOVE));
    }

    public void addReviewEvent(int userId, int reviewId) {
        addEvent(createEvent(userId, reviewId, EventType.REVIEW, EventOperation.ADD));
    }

    public void updateReviewEvent(int userId, int reviewId) {
        addEvent(createEvent(userId, reviewId, EventType.REVIEW, EventOperation.UPDATE));
    }

    public void removeReviewEvent(int userId, int reviewId) {
        addEvent(createEvent(userId, reviewId, EventType.REVIEW, EventOperation.REMOVE));
    }

    public void addFriendEvent(int userId, int friendId) {
        addEvent(createEvent(userId, friendId, EventType.FRIEND, EventOperation.ADD));
    }

    public void removeFriendEvent(int userId, int friendId) {
        addEvent(createEvent(userId, friendId, EventType.FRIEND, EventOperation.REMOVE));
    }

    private Event createEvent(int userId, int entityId, EventType eventType, EventOperation operation) {
        return Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(userId)
                .eventType(eventType)
                .operation(operation)
                .entityId(entityId)
                .build();
    }
}