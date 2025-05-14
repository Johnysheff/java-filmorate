package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.*;
import java.util.*;

@Repository

public class UserDbStorage implements UserStorage {
    private final JdbcTemplate jdbcTemplate;

    private static final String DELETE_USER_QUERY = """
            DELETE FROM USERS
            WHERE user_id = ?
            """;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User addUser(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setString(1, user.getEmail());
            stmt.setString(2, user.getLogin());
            stmt.setString(3, user.getName());
            stmt.setDate(4, Date.valueOf(user.getBirthday()));
            return stmt;
        }, keyHolder);

        user.setId(keyHolder.getKey().intValue());
        return user;
    }

    @Override
    public User updateUser(User user) {
        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        jdbcTemplate.update(sql,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId());

        updateFriends(user);
        return user;
    }

    @Override
    public Optional<User> getUserById(int id) {
        String sql = "SELECT * FROM users WHERE user_id = ?";
        try {
            User user = jdbcTemplate.queryForObject(sql, this::mapRowToUser, id);
            loadFriends(user);
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public void deleteUser(User user) {
        jdbcTemplate.update(DELETE_USER_QUERY, user.getId());
    }

    @Override
    public List<User> getAllUsers() {
        String sql = """
            SELECT
                u.user_id as user_id,
                u.email,
                u.login,
                u.name,
                u.birthday,
                f.friend_id
            FROM users u
            LEFT JOIN friendships f ON u.user_id = f.user_id
            """;

        Map<Integer, User> usersMap = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            int userId = rs.getInt("user_id");

            User user = usersMap.computeIfAbsent(userId, id -> {
                User newUser = new User();
                newUser.setId(id);
                try {
                    newUser.setEmail(rs.getString("email"));
                    newUser.setLogin(rs.getString("login"));
                    newUser.setName(rs.getString("name"));
                    newUser.setBirthday(rs.getDate("birthday").toLocalDate());
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
                newUser.setFriends(new HashSet<>());
                return newUser;
            });

            int friendId = rs.getInt("friend_id");
            if (friendId != 0 && !usersMap.containsKey(friendId)) {
                user.getFriends().add((long) friendId);
            }
        });

        return new ArrayList<>(usersMap.values());
    }

    @Override
    public void addFriend(int userId, int friendId) {
        String sql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public List<User> getFriends(int userId) {
        String sql = "SELECT u.* FROM users u JOIN friendships f ON u.user_id = f.friend_id WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {
        String sql = "SELECT u.* FROM users u " +
                "JOIN friendships f1 ON u.user_id = f1.friend_id " +
                "JOIN friendships f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherId);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {
        return new User(rs.getInt("user_id"),
                rs.getString("email"),
                rs.getString("login"),
                rs.getString("name"),
                rs.getDate("birthday").toLocalDate());
    }

    private void loadFriends(User user) {
        String sql = "SELECT friend_id FROM friendships WHERE user_id = ?";
        List<Long> friends = jdbcTemplate.query(sql,
                (rs, rowNum) -> rs.getLong("friend_id"),
                user.getId());
        user.setFriends(new HashSet<>(friends));
    }

    private void updateFriends(User user) {
        String sql = "DELETE FROM friendships WHERE user_id = ?";
        jdbcTemplate.update(sql, user.getId());

        if (!user.getFriends().isEmpty()) {
            String insertSql = "INSERT INTO friendships (user_id, friend_id) VALUES (?, ?)";
            List<Object[]> batchArgs = new ArrayList<>();
            for (Long friendId : user.getFriends()) {
                batchArgs.add(new Object[]{user.getId(), friendId});
            }
            jdbcTemplate.batchUpdate(insertSql, batchArgs);
        }
    }
}