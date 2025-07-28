package com.matchFit.post.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetPostCalender {
	private String day;
	private Event event;

	
	@AllArgsConstructor
	@Getter
	public static class Event {
		private String sports;
        private int totalEvents;
        private List<String> time;
	}
}
