package ru.yandex.practicum.filmorate.storage.event;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.feed.Event;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class EventDbStorage implements EventStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Event> getEventsByUserId(int userId) {
        String sql = "SELECT * FROM events WHERE user_id = ? ORDER BY event_id ASC";
        try {
            return jdbcTemplate.query(sql, this::mapRowToEvent, userId);
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }
    }

    @Override
    public Event addEvent(Event event) {
        String sql = "INSERT INTO events (user_id, entity_id, event_type, operation, timestamp) VALUES (?, ?, ?, ?, ?)";
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            stmt.setInt(1, event.getUserId());
            stmt.setInt(2, event.getEntityId());
            stmt.setString(3, event.getEventType());
            stmt.setString(4, event.getOperation());
            stmt.setLong(5, event.getTimestamp());
            return stmt;
        }, keyHolder);

        event.setEventId(keyHolder.getKey().intValue());
        return event;
    }

    private Event mapRowToEvent(ResultSet rs, int rowNum) throws SQLException {
        return Event.builder()
                .eventId(rs.getInt("event_id"))
                .userId(rs.getInt("user_id"))
                .entityId(rs.getInt("entity_id"))
                .eventType(rs.getString("event_type"))
                .operation(rs.getString("operation"))
                .timestamp(rs.getLong("timestamp"))
                .build();
    }
}