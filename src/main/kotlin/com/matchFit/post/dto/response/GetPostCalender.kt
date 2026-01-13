package com.matchFit.post.dto.response


data class GetPostCalender(
    val day: String,
    val event: Event
) {
    data class Event(
        val sports: String,
        val totalEvents: Int,
        val time: List<String>
    )
}
