package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Review;
import ru.yandex.practicum.filmorate.service.ReviewService;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    private final ReviewService service;

    @Autowired
    public ReviewController(ReviewService service) {
        this.service = service;
    }

    @PostMapping
    public Review addReview(@Valid @RequestBody Review review) {
        return service.addReview(review);
    }

    @GetMapping("{id}")
    public Review getReviewById(@PathVariable int id) {
        return service.getReviewById(id);
    }

    @DeleteMapping("{id}")
    public void deleteReviewById(@PathVariable int id) {
        service.deleteReviewById(id);
    }

    @PutMapping
    public Review updateReview(@RequestBody Review review) {
        return service.updateReview(review);
    }

    @GetMapping
    public List<Review> getReviews(@RequestParam(required = false) Integer filmId,
                                   @RequestParam(required = false, defaultValue = "10") int count) {
        return service.getReviews(filmId, count);
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable int id, @PathVariable int userId) {
        service.addLike(id, userId);
    }

    @PutMapping("/{id}/dislike/{userId}")
    public void dislike(@PathVariable int id, @PathVariable int userId) {
        service.addDislike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void removeLike(@PathVariable int id, @PathVariable int userId) {
        service.removeReaction(id, userId);
    }

    @DeleteMapping("/{id}/dislike/{userId}")
    public void removeDislike(@PathVariable int id, @PathVariable int userId) {
        service.removeReaction(id, userId);
    }
}
