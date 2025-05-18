package ru.yandex.practicum.filmorate.model.feed;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class Event {
    private Long timestamp;
    private Integer userId;
    private String eventType;
    private String operation;
    private Integer entityId;
    private Integer eventId;
}