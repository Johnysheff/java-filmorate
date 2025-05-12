-- MPA рейтинги
MERGE INTO mpa_ratings (mpa_id, name, description) VALUES (1, 'G', 'Нет возрастных ограничений');
MERGE INTO mpa_ratings (mpa_id, name, description) VALUES (2, 'PG', 'Детям рекомендуется смотреть с родителями');
MERGE INTO mpa_ratings (mpa_id, name, description) VALUES (3, 'PG-13', 'Детям до 13 лет просмотр не желателен');
MERGE INTO mpa_ratings (mpa_id, name, description) VALUES (4, 'R', 'Лицам до 17 лет с родителями');
MERGE INTO mpa_ratings (mpa_id, name, description) VALUES (5, 'NC-17', 'Лицам до 18 лет просмотр запрещён');

-- Жанры
MERGE INTO genres (genre_id, name) VALUES (1, 'Комедия');
MERGE INTO genres (genre_id, name) VALUES (2, 'Драма');
MERGE INTO genres (genre_id, name) VALUES (3, 'Мультфильм');
MERGE INTO genres (genre_id, name) VALUES (4, 'Триллер');
MERGE INTO genres (genre_id, name) VALUES (5, 'Документальный');
MERGE INTO genres (genre_id, name) VALUES (6, 'Боевик');

