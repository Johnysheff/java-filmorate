package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {
    private final Map<Integer, User> users = new HashMap<>();
    private final Map<Integer, Set<Integer>> friends = new HashMap<>();
    private int idCounter = 1;

    @Override
    public User addUser(User user) {
        user.setId(idCounter++);
        users.put(user.getId(), user);
        friends.put(user.getId(), new HashSet<>());
        return user;
    }

    @Override
    public User updateUser(User user) {
        users.put(user.getId(), user);
        return user;
    }

    @Override
    public List<User> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getUserById(int id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public void addFriend(int userId, int friendId) {
        friends.get(userId).add(friendId);
        friends.get(friendId).add(userId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        friends.get(userId).remove(friendId);
        friends.get(friendId).remove(userId);
    }

    //Получаем список друзей конкретного пользователя по id
    @Override
    public List<User> getFriends(int userId) {
        return friends.get(userId).stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();
    }

    //Находим общих друзей
    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        Set<Integer> commonFriendsIds = new HashSet<>(friends.get(userId));
        commonFriendsIds.retainAll(friends.get(otherId));
        return commonFriendsIds.stream()
                .map(users::get)
                .filter(Objects::nonNull)
                .toList();
    }
}