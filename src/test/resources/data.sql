-- Тестовый пользователь
MERGE INTO users (user_id, email, login, name, birthday)
VALUES (1, 'test@mail.ru', 'testlogin', 'Тестовый Пользователь', '2000-01-01');

-- Тестовые режиссеры
MERGE INTO directors (director_id, name) VALUES (1, 'Тестовый Режиссер 1');
MERGE INTO directors (director_id, name) VALUES (2, 'Тестовый Режиссер 2');

-- Тестовый фильм
MERGE INTO films (film_id, name, description, release_date, duration, mpa_id)
VALUES (1, 'Тестовый Фильм', 'Описание тестового фильма', '2020-01-01', 120, 1);

-- Связь фильма и режиссера
MERGE INTO film_directors (film_id, director_id) VALUES (1, 1);

--Лайки для тестов
MERGE INTO film_likes (film_id, user_id) VALUES (1, 1);