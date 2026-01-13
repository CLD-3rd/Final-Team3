package com.matchFit.post.policy

import com.matchFit.post.entity.Post
import com.matchFit.post.entity.Status
import java.time.LocalDate


object PostNotificationPolicy {
    fun isMatchTomorrow(post: Post): Boolean =
        post.status == Status.CLOSED && post.date.toLocalDate() == LocalDate.now().plusDays(1)
}
