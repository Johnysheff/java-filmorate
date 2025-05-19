package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.review.ReviewRepository;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository repository;
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final EventService eventService;

    public Review addReview(Review review) {
        validateUserId(review.getUserId());
        validateFilmId(review.getFilmId());
        Review addedReview = repository.addReview(review);
        eventService.addReviewEvent(review.getUserId(), addedReview.getReviewId());
        return addedReview;
    }

    public Review getReviewById(long id) {
        return repository.findReviewById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));
    }

    public Review updateReview(Review review) {
        Review updatedReview = repository.updateReview(review);
        eventService.updateReviewEvent(updatedReview.getUserId(), updatedReview.getReviewId());
        return updatedReview;
    }

    public void deleteReviewById(int id) {
        Review deleted = repository.findReviewById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв с id: " + id + " не найден."));
        repository.deleteReviewById(id);
        eventService.removeReviewEvent(deleted.getUserId(), deleted.getReviewId());
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
        userStorage.getUserById(id).orElseThrow(() ->
                new NotFoundException("Пользователь с id=" + id + " не найден."));
    }

    public void validateFilmId(Integer id) {
        if (id == null) {
            throw new ValidationException("Id фильма не может быть null.");
        }
        filmStorage.getFilmById(id).orElseThrow(() ->
                new NotFoundException("Фильм с id=" + id + " не найден."));
    }
}