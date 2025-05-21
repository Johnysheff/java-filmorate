package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;
    private final RecommendationService recommendationService;
    private final EventService eventService;

    @Autowired
    public UserService(UserStorage userStorage, RecommendationService recommendationService, EventService eventService) {
        this.userStorage = userStorage;
        this.recommendationService = recommendationService;
        this.eventService = eventService;

    }

    public User addUser(User user) {
        return userStorage.addUser(user);
    }

    public User updateUser(User user) {
        Optional<User> foundedUser = userStorage.getUserById(user.getId());
        if (foundedUser.isEmpty()) {
            throw new NotFoundException("Отсутствует пользователь");
        }
        return userStorage.updateUser(user);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id).orElseThrow(() ->
                new NotFoundException("Пользователь с id " + id + " не найден"));
    }

    public User deleteUser(Integer userId) {
        User deletedUser = userStorage.getUserById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id " + userId + " не найден"));
        userStorage.deleteUser(deletedUser);
        return deletedUser;
    }

    public void addFriend(int userId, int friendId) {
        Optional<User> foundedUser = userStorage.getUserById(userId);
        if (foundedUser.isEmpty()) {
            throw new NotFoundException("Отсутствует пользователь");
        }
        Optional<User> foundedFriend = userStorage.getUserById(friendId);
        if (foundedFriend.isEmpty()) {
            throw new NotFoundException("Отсутствует друг");
        }
        userStorage.addFriend(userId, friendId);

        eventService.addFriendEvent(userId, friendId);

        log.info("Пользователь {} добавил в друзья пользователя {}", userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        Optional<User> foundedUser = userStorage.getUserById(userId);
        if (foundedUser.isEmpty()) {
            throw new NotFoundException("Отсутствует пользователь");
        }
        Optional<User> foundedFriend = userStorage.getUserById(friendId);
        if (foundedFriend.isEmpty()) {
            throw new NotFoundException("Отсутствует друг");
        }
        userStorage.removeFriend(userId, friendId);

        eventService.removeFriendEvent(userId, friendId);

        log.info("Пользователь {} удалил из друзей пользователя {}", userId, friendId);
    }

    public List<User> getFriends(int userId) {
        Optional<User> foundedUser = userStorage.getUserById(userId);
        if (foundedUser.isEmpty()) {
            throw new NotFoundException("Отсутствует пользователь");
        }
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int userId, int otherId) {
        return userStorage.getCommonFriends(userId, otherId);
    }

    public Collection<Film> getRecommendations(Integer userId) {
        Collection<Film> films = recommendationService.getRecommendations(userId);
        return new ArrayList<>(films);
    }
}