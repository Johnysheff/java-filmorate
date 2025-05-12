package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.sql.Date;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;

@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final DirectorStorage directorStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;

    private static final String GET_FILM_IDS_BY_USER_ID_QUERY = "SELECT film_id FROM FILM_LIKES WHERE user_id = ?";
    private static final String GET_USER_IDS_BY_FILM_ID_QUERY = "SELECT user_id FROM FILM_LIKES WHERE film_id = ?";
    private static final String DELETE_FILM_QUERY = "DELETE FROM FILMS WHERE film_id = ?";

    public FilmDbStorage(JdbcTemplate jdbcTemplate, DirectorStorage directorStorage, MpaStorage mpaStorage, GenreStorage genreStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.directorStorage = directorStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
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
        jdbcTemplate.update("DELETE FROM film_directors WHERE film_id = ?", film.getId());

        return film;
    }

    @Override
    public Optional<Film> getFilmById(int id) {
        String sql = """
                SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id,
                m.name AS mpa_name, m.description AS mpa_description, g.genre_id, g.name AS genre_name,
                d.director_id, d.name AS director_name
                FROM films f
                JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
                LEFT JOIN film_genres fg ON f.film_id = fg.film_id
                LEFT JOIN genres g ON fg.genre_id = g.genre_id
                LEFT JOIN film_directors fd ON f.film_id = fd.film_id
                LEFT JOIN directors d ON fd.director_id = d.director_id
                WHERE f.film_id = ?
                """;
        try {
            Film film = jdbcTemplate.query(sql, rs -> {
                Film currentFilm = null;
                Set<Genre> genres = new HashSet<>();
                Set<Director> directors = new HashSet<>();

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
                    }

                    int genreId = rs.getInt("genre_id");
                    if (!rs.wasNull()) {
                        genres.add(new Genre(
                                genreId,
                                rs.getString("genre_name")
                        ));
                    }

                    int directorId = rs.getInt("director_id");
                    if (!rs.wasNull()) {
                        directors.add(new Director(
                                directorId,
                                rs.getString("director_name")
                        ));
                    }
                }

                if (currentFilm != null) {
                    currentFilm.setGenres(new ArrayList<>(genres));
                    currentFilm.setDirectors(new ArrayList<>(directors));
                }
                return currentFilm;
            }, id);
            return Optional.ofNullable(film);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteFilm(Film film) {
        jdbcTemplate.update(DELETE_FILM_QUERY, film.getId());
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
        return getPopularFilmsWithFilters(count, null, null);
    }

    @Override
    public List<Film> getPopularFilmsWithFilters(int count, Integer genreId, Integer year) {
        StringBuilder sql = new StringBuilder("""
                SELECT f.*, m.name AS mpa_name, m.description AS mpa_description,
                COUNT(fl.user_id) AS likes_count
                FROM films f
                LEFT JOIN film_likes fl ON f.film_id = fl.film_id
                JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
                """);

        boolean hasWhere = false;

        if (genreId != null || year != null) {
            sql.append(" WHERE ");
            if (genreId != null) {
                sql.append("EXISTS (SELECT 1 FROM film_genres fg WHERE fg.film_id = f.film_id AND fg.genre_id = ").append(genreId).append(")");
                hasWhere = true;
            }
            if (year != null) {
                if (hasWhere) {
                    sql.append(" AND ");
                }
                sql.append("EXTRACT(YEAR FROM f.release_date) = ").append(year);
            }
        }

        sql.append(" GROUP BY f.film_id ORDER BY likes_count DESC LIMIT ?");
        return jdbcTemplate.query(sql.toString(), (rs, rowNum) -> {
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

    @Override
    public List<Film> getCommonFilms(int userId1, int userId2) {
        String query = """
                SELECT f.*
                FROM FILMS f
                JOIN film_likes fl1 ON f.film_id = fl1.film_id AND fl1.user_id = ?
                JOIN film_likes fl2 ON f.film_id = fl2.film_id AND fl2.user_id = ?
                LEFT JOIN film_likes fl_likes ON f.film_id = fl_likes.film_id
                GROUP BY f.film_id
                ORDER BY COUNT(fl_likes.user_id) DESC
                """;
        return jdbcTemplate.query(query, new FilmRowMapper(), userId1, userId2);
    }

    @Override
    public Map<Integer, List<Genre>> getAllGenres(Collection<Film> films) {
        Map<Integer, List<Genre>> genres = new HashMap<>();

        List<Integer> filmIds = films.stream()
                .map(Film::getId)
                .toList();

        String inSql = String.join(",", Collections.nCopies(filmIds.size(), "?"));

        String query = String.format("""
                SELECT fg.film_id, g.genre_id, g.name
                FROM film_genres fg
                JOIN genres g ON g.genre_id = fg.genre_id
                WHERE fg.film_id IN (%s)
                """, inSql);

        jdbcTemplate.query(query, filmIds.toArray(), rs -> {
            int filmId = rs.getInt("film_id");
            Genre genre = new Genre(
                    rs.getInt("genre_id"),
                    rs.getString("name")
            );
            genres.computeIfAbsent(filmId, k -> new ArrayList<>()).add(genre);
        });
        return genres;
    }

    public List<Integer> getFilmIdsByUserId(Integer userId) {
        return jdbcTemplate.queryForList(GET_FILM_IDS_BY_USER_ID_QUERY, Integer.class, userId);
    }

    public List<Integer> getUserIdsByFilmId(Integer filmId) {
        return jdbcTemplate.queryForList(GET_USER_IDS_BY_FILM_ID_QUERY, Integer.class, filmId);
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByYear(int directorId) {
        String sql = """
                SELECT f.*, m.name AS mpa_name, m.description AS mpa_description
                FROM films f
                JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
                JOIN film_directors fd ON f.film_id = fd.film_id
                WHERE fd.director_id = ?
                ORDER BY f.release_date
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = createFilmFromResultSet(rs);
            List<Director> directors = directorStorage.getDirectorsByFilmId(film.getId());
            film.setDirectors(directors != null ? directors : Collections.emptyList());
            return film;
        }, directorId);
    }

    @Override
    public List<Film> getFilmsByDirectorSortedByLikes(int directorId) {
        String sql = """
                SELECT f.*, m.name AS mpa_name, m.description AS mpa_description,
                COUNT(fl.user_id) AS likes_count
                FROM films f
                JOIN mpa_ratings m ON f.mpa_id = m.mpa_id
                JOIN film_directors fd ON f.film_id = fd.film_id
                LEFT JOIN film_likes fl ON f.film_id = fl.film_id
                WHERE fd.director_id = ?
                GROUP BY f.film_id
                ORDER BY likes_count DESC
                """;
        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            Film film = createFilmFromResultSet(rs);
            List<Director> directors = directorStorage.getDirectorsByFilmId(film.getId());
            film.setDirectors(directors != null ? directors : Collections.emptyList());
            return film;
        }, directorId);
    }

    private Film createFilmFromResultSet(ResultSet rs) throws SQLException {
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
    }

    public List<Film> searchFilmsByDirector(String directorName) {
        String sql =
                "SELECT f.* " +
                        "FROM films f " +
                        "JOIN film_directors fd ON f.film_id = fd.film_id " +
                        "JOIN directors d ON fd.director_id = d.director_id " +
                        "WHERE LOWER(d.name) LIKE LOWER(?)";

        String searchPattern = "%" + directorName + "%";

        return jdbcTemplate.query(sql, new Object[]{searchPattern}, (rs, rowNum) -> {
            int filmId = rs.getInt("film_id");
            String name = rs.getString("name");
            String description = rs.getString("description");
            LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
            int duration = rs.getInt("duration");
            int mpaId = rs.getInt("mpa_id");

            Optional<MpaRating> optionalMpa = mpaStorage.getMpaRatingById(mpaId);
            MpaRating mpa = optionalMpa.orElse(null);

            List<Genre> genres = genreStorage.getGenresByFilmId(filmId);
            List<Director> directors = directorStorage.getDirectorsByFilmId(filmId);

            return new Film(filmId, name, description, releaseDate, duration, mpa, genres, directors);
        });
    }

    public List<Film> searchFilmsByTitle(String title) {
        String sql =
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
                        "FROM films f " +
                        "WHERE LOWER(f.name) LIKE LOWER(?)";

        String searchPattern = "%" + title + "%";

        return jdbcTemplate.query(sql, new Object[]{searchPattern}, (rs, rowNum) -> {
            int filmId = rs.getInt("film_id");
            String filmName = rs.getString("name");
            String description = rs.getString("description");
            LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
            int duration = rs.getInt("duration");
            int mpaId = rs.getInt("mpa_id");

            Optional<MpaRating> optionalMpa = mpaStorage.getMpaRatingById(mpaId);
            MpaRating mpa = optionalMpa.orElse(null);

            List<Genre> genres = genreStorage.getGenresByFilmId(filmId);
            List<Director> directors = directorStorage.getDirectorsByFilmId(filmId);

            return new Film(filmId, filmName, description, releaseDate, duration, mpa, genres, directors);
        });
    }

    public List<Film> searchFilmsByTitleAndDirector(String title, String directorName) {
        String sql =
                "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id " +
                        "FROM films f " +
                        "LEFT JOIN film_directors fd ON f.film_id = fd.film_id " +
                        "LEFT JOIN directors d ON fd.director_id = d.director_id " +
                        "WHERE " +
                        "LOWER(f.name) LIKE LOWER(?) " +
                        "OR LOWER(d.name) LIKE LOWER(?)";

        String titlePattern = "%" + title + "%";
        String directorPattern = "%" + directorName + "%";

        return jdbcTemplate.query(sql, new Object[]{titlePattern, directorPattern}, (rs, rowNum) -> {
            int filmId = rs.getInt("film_id");
            String filmName = rs.getString("name");
            String description = rs.getString("description");
            LocalDate releaseDate = rs.getDate("release_date").toLocalDate();
            int duration = rs.getInt("duration");
            int mpaId = rs.getInt("mpa_id");

            Optional<MpaRating> optionalMpa = mpaStorage.getMpaRatingById(mpaId);
            MpaRating mpa = optionalMpa.orElse(null);

            List<Genre> genres = genreStorage.getGenresByFilmId(filmId);
            List<Director> directors = directorStorage.getDirectorsByFilmId(filmId);

            return new Film(filmId, filmName, description, releaseDate, duration, mpa, genres, directors);
        });
    }
}