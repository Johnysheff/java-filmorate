package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository

public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;


    public FilmDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;

    }

    @Override
    public Film addFilm(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, film.getName());
            stmt.setString(2, film.getDescription());
            stmt.setDate(3, Date.valueOf(film.getReleaseDate()));
            stmt.setInt(4, film.getDuration());
            stmt.setInt(5, film.getMpa().getId());
            return stmt;
        }, keyHolder);

        film.setId(keyHolder.getKey().intValue());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE film_id = ?";
        jdbcTemplate.update(sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId());

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());


        return film;
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        String sql = """ 
                SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, 
                m.name AS mpa_name, m.description AS mpa_description, g.genre_id, g.name AS genre_name 
                FROM films f JOIN mpa_ratings m ON f.mpa_id = m.mpa_id 
                LEFT JOIN film_genres fg ON f.film_id = fg.film_id 
                LEFT JOIN genres g ON fg.genre_id = g.genre_id 
                WHERE f.film_id = ? 
                """;
        try {
            Film film = jdbcTemplate.query(sql, rs -> {
                Film currentFilm = null;
                while (rs.next()) {
                    if (currentFilm == null) {
                        currentFilm = new Film(
                                rs.getString("name"),
                                rs.getString("description"),
                                rs.getDate("release_date").toLocalDate(),
                                rs.getInt("duration")
                        );
                        currentFilm.setId(rs.getInt("film_id"));
                        currentFilm.setMpa(new MpaRating(
                                rs.getInt("mpa_id"),
                                rs.getString("mpa_name"),
                                rs.getString("mpa_description")
                        ));

                        currentFilm.setGenres(new ArrayList<>());
                    }

                    int genreId = rs.getInt("genre_id");
                    if (!rs.wasNull()) {
                        Genre genre = new Genre(
                                genreId,
                                rs.getString("genre_name")
                        );

                        if (currentFilm.getGenres().stream().noneMatch(g -> g.getId() == genreId)) {
                            currentFilm.getGenres().add(genre);
                        }
                    }
                }
                return currentFilm;
            }, id);
            return Optional.ofNullable(film);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Film> getAllFilms() {
        String sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description " +
                "FROM films f JOIN mpa_ratings m ON f.mpa_id = m.mpa_id";
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film(
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration")
            );
            film.setId(rs.getInt("film_id"));
            film.setMpa(new MpaRating(
                    rs.getInt("mpa_id"),
                    rs.getString("mpa_name"),
                    rs.getString("mpa_description")
            ));

            return film;
        });
    }

    @Override
    public void addLike(int filmId, int userId) {
        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    @Override
    public List<Film> getPopularFilms(int count) {
        String sql = "SELECT f.*, m.name AS mpa_name, m.description AS mpa_description, " +
                "COUNT(fl.user_id) AS likes_count " +
                "FROM films f LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
                "JOIN mpa_ratings m ON f.mpa_id = m.mpa_id " +
                "GROUP BY f.film_id ORDER BY likes_count DESC LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = new Film(
                    rs.getString("name"),
                    rs.getString("description"),
                    rs.getDate("release_date").toLocalDate(),
                    rs.getInt("duration")
            );
            film.setId(rs.getInt("film_id"));
            film.setMpa(new MpaRating(
                    rs.getInt("mpa_id"),
                    rs.getString("mpa_name"),
                    rs.getString("mpa_description")
            ));

            return film;
        }, count);
    }
}