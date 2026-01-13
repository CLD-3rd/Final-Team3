package com.matchFit.post.dto

import com.matchFit.post.entity.Post
import com.matchFit.post.entity.Sports
import com.matchFit.post.entity.Status
import com.matchFit.post.entity.Town
import com.matchFit.user.entity.Gender
import com.matchFit.user.entity.User
import java.time.LocalDateTime

class PostRequestDto {
    var id: Long? = null
    var title: String? = null
    var description: String? = null
    var location: String? = null
    var imageUrl: String? = null
    var gender: Gender? = null
    var sports: Sports? = null
    var cost: Int? = null
    var status: Status? = null
    var town: Town? = null
    var maxPeople: Int? = null
    var date: LocalDateTime? = null

    fun toEntity(user: User): Post {
        val post = Post()
        post.title = title ?: ""
        post.description = description ?: ""
        post.location = location ?: ""
        post.imageUrl = imageUrl
        post.gender = gender ?: throw IllegalArgumentException("gender is required")
        post.sports = sports ?: throw IllegalArgumentException("sports is required")
        post.cost = cost ?: 0
        post.status = status ?: Status.OPEN
        post.town = town ?: throw IllegalArgumentException("town is required")
        post.maxPeople = maxPeople ?: 0
        post.date = date ?: throw IllegalArgumentException("date is required")
        post.user = user
        return post
    }
}
