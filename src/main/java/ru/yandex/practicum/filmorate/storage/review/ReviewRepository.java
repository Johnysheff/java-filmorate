package ru.yandex.practicum.filmorate.storage.review;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Review;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class ReviewRepository {
    private final JdbcTemplate jdbc;
    private final RowMapper<Review> mapper;

    public ReviewRepository(JdbcTemplate jdbc, RowMapper<Review> mapper) {
        this.jdbc = jdbc;
        this.mapper = mapper;
    }

    public Review addReview(Review review) {

        String sql = "INSERT INTO reviews (content, is_positive, user_id, film_id) " +
                "VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbc.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, review.getContent());
            stmt.setBoolean(2, review.getIsPositive());
            stmt.setInt(3, review.getUserId());
            stmt.setInt(4, review.getFilmId());
            return stmt;
        }, keyHolder);

        Integer id = keyHolder.getKeyAs(Integer.class);
        if (id != null) {
            review.setReviewId(id);
        } else {
            throw new InternalServerException("Не удалось сохранить данные.");
        }
        return review;
    }

    public Review updateReview(Review review) {
        String sql = """
                UPDATE reviews
                SET content = ?, is_positive = ?
                WHERE review_id = ?;
                """;

        jdbc.update(sql,
                review.getContent(),
                review.getIsPositive(),
                review.getReviewId()
        );

        return findReviewById(review.getReviewId()).orElseThrow(() -> new InternalServerException("Не удалось обновить отзыв"));
    }

    public Optional<Review> findReviewById(long id) {
        String sql = "SELECT * FROM reviews WHERE review_id = ?";
        return jdbc.query(sql, mapper, id)
                .stream()
                .findFirst();
    }

    public int deleteReviewById(long id) {
        String sql = "DELETE from reviews WHERE review_id = ?";
        return jdbc.update(sql, id);
    }

    public List<Review> findAll(int limit) {
        String sql = "SELECT * FROM reviews ORDER BY useful DESC LIMIT ?";
        return jdbc.query(sql, mapper, limit);
    }

    public List<Review> findFilmById(int filmId, int limit) {
        String sql = "SELECT * FROM reviews WHERE film_id = ? ORDER BY useful DESC LIMIT ?";
        return jdbc.query(sql, mapper, filmId, limit);
    }

    public void addLike(int reviewId, int userId) {
        String sql = """
                MERGE INTO review_likes (review_id, user_id, is_like)
                KEY (review_id, user_id)
                VALUES(?,?,true)
                """;

        jdbc.update(sql, reviewId, userId);
    }

    public void addDislike(int reviewId, int userId) {
        String sql = """
                MERGE INTO review_likes (review_id, user_id, is_like)
                KEY (review_id, user_id)
                VALUES(?,?,false)
                """;

        jdbc.update(sql, reviewId, userId);
    }

    public void removeReaction(int reviewId, int userId) {
        String sql = "DELETE FROM review_likes WHERE review_id = ? AND user_id = ?";
        jdbc.update(sql, reviewId, userId);
    }

    public void updateReviewUseful(int reviewId) {
        String sql = """
                UPDATE reviews
                SET useful = COALESCE((
                    SELECT SUM(CASE WHEN is_like THEN 1 ELSE -1 END)
                    FROM review_likes
                    WHERE review_id = ?
                ), 0)
                WHERE review_id = ?
                """;

        jdbc.update(sql, reviewId, reviewId);
    }
}
