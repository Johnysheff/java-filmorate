package ru.yandex.practicum.filmorate.storage;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD,
        scripts = "classpath:clear_tables.sql")
class UserDbStorageTest {
    @Autowired
    private UserDbStorage userStorage;

    @Test
    void shouldAddAndFindUserById() {
        User user = new User("evgen.shevtsov@yandex.ru", "johnyshef", "Евгений Шевцов",
                LocalDate.of(1990, 12, 2));
        User addedUser = userStorage.addUser(user);

        Optional<User> foundUser = userStorage.getUserById(addedUser.getId());
        assertThat(foundUser)
                .isPresent()
                .hasValueSatisfying(u ->
                        assertThat(u)
                                .hasFieldOrPropertyWithValue("email", "evgen.shevtsov@yandex.ru")
                                .hasFieldOrPropertyWithValue("login", "johnyshef")
                );
    }
}