package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RecommendationService {
    private final FilmDbStorage filmRepository;
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    public Collection<Film> getRecommendations(Integer userId) {
        log.info("Получение рекомендаций для пользователя {}", userId);

        Set<Integer> userLikedFilms = new HashSet<>(filmRepository.getFilmIdsByUserId(userId));
        log.debug("Пользователь {} лайкнул фильмы: {}", userId, userLikedFilms);

        if (userLikedFilms.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Integer, Set<Integer>> similarUsers = findSimilarUsers(userId, userLikedFilms);
        if (similarUsers.isEmpty()) {
            return Collections.emptyList();
        }

        Set<Integer> recommendedFilmIds = getRecommendedFilmIds(userLikedFilms, similarUsers);
        return convertToFilms(recommendedFilmIds);
    }

    private Map<Integer, Set<Integer>> findSimilarUsers(Integer userId, Set<Integer> userLikedFilms) {
        String inSql = String.join(",", Collections.nCopies(userLikedFilms.size(), "?"));
        String sql = String.format("""
                SELECT user_id, film_id FROM film_likes 
                WHERE film_id IN (%s) AND user_id != ?
                """, inSql);

        Map<Integer, Set<Integer>> similarUsers = new HashMap<>();
        List<Object> params = new ArrayList<>(userLikedFilms);
        params.add(userId);

        jdbcTemplate.query(sql, rs -> {
            int otherUserId = rs.getInt("user_id");
            int filmId = rs.getInt("film_id");
            similarUsers.computeIfAbsent(otherUserId, k -> new HashSet<>()).add(filmId);
        }, params.toArray());

        return similarUsers;
    }

    private Set<Integer> getRecommendedFilmIds(Set<Integer> userLikedFilms,
                                               Map<Integer, Set<Integer>> similarUsers) {
        int maxIntersection = similarUsers.values().stream()
                .mapToInt(Set::size)
                .max()
                .orElse(0);

        return similarUsers.entrySet().stream()
                .filter(entry -> entry.getValue().size() == maxIntersection)
                .flatMap(entry -> {
                    Set<Integer> films = new HashSet<>(
                            filmRepository.getFilmIdsByUserId(entry.getKey()));
                    films.removeAll(userLikedFilms);
                    return films.stream();
                })
                .collect(Collectors.toSet());
    }

    private List<Film> convertToFilms(Set<Integer> filmIds) {
        if (filmIds.isEmpty()) {
            return Collections.emptyList();
        }
        return filmRepository.getFilmsByIds(new ArrayList<>(filmIds));
    }
}