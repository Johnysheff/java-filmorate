-- Очистка всех таблиц перед тестами
DELETE FROM film_directors;
DELETE FROM film_genres;
DELETE FROM film_likes;
DELETE FROM friendships;
DELETE FROM films;
DELETE FROM users;
DELETE FROM directors;
DELETE FROM genres;
DELETE FROM mpa_ratings;

-- Инициализация MPA рейтингов
INSERT INTO mpa_ratings (mpa_id, name, description) VALUES
(1, 'G', 'Нет возрастных ограничений'),
(2, 'PG', 'Детям рекомендуется смотреть с родителями'),
(3, 'PG-13', 'Детям до 13 лет просмотр не желателен'),
(4, 'R', 'Лицам до 17 лет с родителями'),
(5, 'NC-17', 'Лицам до 18 лет просмотр запрещён');

-- Инициализация жанров
INSERT INTO genres (genre_id, name) VALUES
(1, 'Комедия'),
(2, 'Драма'),
(3, 'Мультфильм'),
(4, 'Триллер'),
(5, 'Документальный'),
(6, 'Боевик');