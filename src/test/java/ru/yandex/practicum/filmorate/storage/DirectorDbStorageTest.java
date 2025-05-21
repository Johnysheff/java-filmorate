package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = "classpath:clear_tables.sql")
class DirectorDbStorageTest {
    private final DirectorDbStorage directorStorage;

    @Test
    void shouldAddAndFindDirectorById() {
        Director director = new Director(null, "Test Director");
        Director addedDirector = directorStorage.addDirector(director);

        Optional<Director> foundDirector = directorStorage.getDirectorById(addedDirector.getId());

        assertThat(foundDirector)
                .isPresent()
                .hasValueSatisfying(d ->
                        assertThat(d)
                                .hasFieldOrPropertyWithValue("name", "Test Director")
                );
    }

    @Test
    void shouldUpdateDirector() {
        Director director = new Director(null, "Test Director");
        Director addedDirector = directorStorage.addDirector(director);

        Director updatedDirector = new Director(addedDirector.getId(), "Updated Name");
        directorStorage.updateDirector(updatedDirector);

        Optional<Director> foundDirector = directorStorage.getDirectorById(addedDirector.getId());

        assertThat(foundDirector)
                .isPresent()
                .hasValueSatisfying(d ->
                        assertThat(d)
                                .hasFieldOrPropertyWithValue("name", "Updated Name")
                );
    }

    @Test
    void shouldDeleteDirector() {
        Director director = new Director(null, "Test Director");
        Director addedDirector = directorStorage.addDirector(director);

        directorStorage.deleteDirector(addedDirector.getId());

        Optional<Director> foundDirector = directorStorage.getDirectorById(addedDirector.getId());
        assertThat(foundDirector).isEmpty();
    }

    @Test
    void shouldGetAllDirectors() {
        // Добавляем тестовых режиссеров
        Director director1 = directorStorage.addDirector(new Director(null, "Режиссер 1"));
        Director director2 = directorStorage.addDirector(new Director(null, "Режиссер 2"));

        List<Director> directors = directorStorage.getAllDirectors();

        assertThat(directors)
                .hasSize(2)
                .extracting(Director::getName)
                .containsExactlyInAnyOrder("Режиссер 1", "Режиссер 2");
    }
}