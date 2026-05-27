package com.matchFit.post.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetPostCalender {
    private final String day;
    private final Event event;

    @Getter
    @AllArgsConstructor
    public static class Event {
        private final String sports;
        private final int totalEvents;
        private final List<String> time;
    }
}
