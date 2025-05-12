package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.review.ReviewRepository;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository repository;

    @Autowired
    public ReviewService(ReviewRepository repository, FilmDbStorage storage, UserDbStorage userStorage) {
        this.repository = repository;
    }

    public Review addReview(Review review) {
        return repository.addReview(review);
    }

    public void deleteReviewById(int id) {
        int rowsAffected = repository.deleteReviewById(id);
        if (rowsAffected == 0) {
            throw new NotFoundException("Отзыв с id: " + id + " не найден.");
        }
    }

    public Review getReviewById(long id) {
        return repository.findReviewById(id)
                .orElseThrow(() -> new NotFoundException("Отзыв не найден"));
    }

    public Review updateReview(Review review) {
        return repository.updateReview(review);
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
}
