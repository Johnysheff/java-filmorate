package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.model.feed.Event;
import ru.yandex.practicum.filmorate.model.feed.EventOperation;
import ru.yandex.practicum.filmorate.model.feed.EventType;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewRepository;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository repository;
    private final EventService eventService;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;

    @Autowired
    public ReviewService(ReviewRepository repository,
                         FilmDbStorage storage,
                         UserDbStorage userStorage,
                         EventService eventService) {
        this.repository = repository;
        this.eventService = eventService;
        this.userStorage = userStorage;
        this.filmStorage = storage;
    }

    public Review addReview(Review review) {
        validateUserId(review.getUserId());
        validateFilmId(review.getFilmId());

        Review addedReview = repository.addReview(review);

        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(review.getUserId())
                .eventType(EventType.REVIEW)
                .operation(EventOperation.ADD)
                .entityId(addedReview.getReviewId())
                .build();
        eventService.addEvent(event);

        return addedReview;
    }

    public Review getReviewById(long id) {
        return repository.findReviewById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));
    }

    public void deleteReviewById(int id) {
        Review review = repository.findReviewById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id: " + id + " не найден."));
        int rowsAffected = repository.deleteReviewById(id);
        if (rowsAffected == 0) {
            throw new NotFoundException("Отзыв с id: " + id + " не найден.");
        }

        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(review.getUserId())
                .eventType(EventType.REVIEW)
                .operation(EventOperation.REMOVE)
                .entityId(review.getReviewId())
                .build();
        eventService.addEvent(event);
    }

    public Review updateReview(Review review) {
        Review updatedReview = repository.updateReview(review);

        Event event = Event.builder()
                .timestamp(System.currentTimeMillis())
                .userId(review.getUserId())
                .eventType(EventType.REVIEW)
                .operation(EventOperation.UPDATE)
                .entityId(review.getReviewId())
                .build();
        eventService.addEvent(event);

        return updatedReview;
    }


    public List<Review> getReviews(Integer filmId, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Параметр count должен быть положительным числом");
        }

        if (filmId == null) {
            return repository.findAll(count);
        }

        return repository.findFilmById(filmId, count);
    }

    public void addLike(int reviewId, int userId) {
        repository.addLike(reviewId, userId);
        repository.updateReviewUseful(reviewId);
    }

    public void addDislike(int reviewId, int userId) {
        repository.addDislike(reviewId, userId);
        repository.updateReviewUseful(reviewId);
    }

    public void removeReaction(int reviewId, int userId) {
        repository.removeReaction(reviewId, userId);
        repository.updateReviewUseful(reviewId);
    }

    public void validateUserId(Integer id) {
        if (id == null) {
            throw new ValidationException("Id пользователя не может быть null.");
        }
        userStorage.getUserById(id).orElseThrow(() -> new NotFoundException("Пользователь с id=" + id + " не найден."));
    }

    public void validateFilmId(Integer id) {
        if (id == null) {
            throw new ValidationException("Id фильма не может быть null.");
        }
        filmStorage.getFilmById(id).orElseThrow(() -> new NotFoundException("Фильм с id=" + id + " не найден."));
    }
}
