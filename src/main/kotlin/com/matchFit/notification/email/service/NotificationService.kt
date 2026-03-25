package com.matchFit.notification.email.service

import com.matchFit.post.entity.Post
import com.matchFit.user.entity.User


interface NotificationService {
    fun sendMatchReminder(user: User, post: Post, postUrl: String)
}
