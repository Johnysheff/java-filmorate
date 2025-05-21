package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.feed.Event;
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
                .orElseThrow(() -> new IllegalArgumentException("Пользователь с id=" + userId + " не найден"));
        return eventStorage.getEventsByUserId(userId);
    }

    public void addEvent(Event event) {
        eventStorage.addEvent(event);
    }

    public void addLikeEvent(int userId, int filmId) {
        addEvent(createEvent(userId, filmId, "LIKE", "ADD"));
    }

    public void removeLikeEvent(int userId, int filmId) {
        addEvent(createEvent(userId, filmId, "LIKE", "REMOVE"));
    }

    public void addFriendEvent(int userId, int friendId) {
        addEvent(createEvent(userId, friendId, "FRIEND", "ADD"));
    }

    public void removeFriendEvent(int userId, int friendId) {
        addEvent(createEvent(userId, friendId, "FRIEND", "REMOVE"));
    }

    public void addReviewEvent(int userId, int reviewId) {
        addEvent(createEvent(userId, reviewId, "REVIEW", "ADD"));
    }

    public void updateReviewEvent(int userId, int reviewId) {
        addEvent(createEvent(userId, reviewId, "REVIEW", "UPDATE"));
    }

    public void removeReviewEvent(int userId, int reviewId) {
        addEvent(createEvent(userId, reviewId, "REVIEW", "REMOVE"));
    }

    private Event createEvent(int userId, int entityId, String eventType, String operation) {
        return Event.builder()
                .userId(userId)
                .entityId(entityId)
                .eventType(eventType)
                .operation(operation)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}