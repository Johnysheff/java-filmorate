package ru.yandex.practicum.filmorate.storage.director;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Director> getAllDirectors() {
        String sql = "SELECT * FROM directors ORDER BY director_id";
        return jdbcTemplate.query(sql, (rs, rowNum) ->
                new Director(rs.getInt("director_id"), rs.getString("name"))
        );
    }

    @Override
    public Optional<Director> getDirectorById(int id) {
        String sql = "SELECT * FROM directors WHERE director_id = ?";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, (rs, rowNum) ->
                    new Director(rs.getInt("director_id"), rs.getString("name")), id));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Director> getDirectorsByIds(List<Integer> ids) {
        if (ids.isEmpty()) {
            return Collections.emptyList();
        }

        String inSql = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = String.format("SELECT * FROM directors WHERE director_id IN (%s)", inSql);

        return jdbcTemplate.query(sql, (rs, rowNum) ->
                        new Director(rs.getInt("director_id"), rs.getString("name")),
                ids.toArray());
    }

    @Override
    public Director addDirector(Director director) {
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, director.getName());
            return stmt;
        }, keyHolder);

        director.setId(keyHolder.getKey().intValue());
        return director;
    }

    @Override
    public Director updateDirector(Director director) {
        String sql = "UPDATE directors SET name = ? WHERE director_id = ?";
        jdbcTemplate.update(sql, director.getName(), director.getId());
        return director;
    }

    @Override
    public void deleteDirector(int id) {
        String sql = "DELETE FROM directors WHERE director_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public List<Director> getDirectorsByFilmId(int filmId) {
        String sql = "SELECT d.* FROM directors d JOIN film_directors fd ON d.director_id = fd.director_id WHERE fd.film_id = ?";
        try {
            return jdbcTemplate.query(sql, (rs, rowNum) ->
                    new Director(rs.getInt("director_id"), rs.getString("name")), filmId);
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void addDirectorsToFilm(int filmId, List<Director> directors) {
        if (directors == null || directors.isEmpty()) {
            return;
        }
        String sql = "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)";
        List<Object[]> batchArgs = directors.stream()
                .map(director -> new Object[]{filmId, director.getId()})
                .toList();
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }

    @Override
    public void deleteDirectorsFromFilm(int filmId) {
        String sql = "DELETE FROM film_directors WHERE film_id = ?";
        jdbcTemplate.update(sql, filmId);
    }
}