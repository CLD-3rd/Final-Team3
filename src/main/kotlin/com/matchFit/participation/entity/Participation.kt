package com.matchFit.participation.entity

import com.matchFit.common.entity.BaseEntity
import com.matchFit.post.entity.Post
import com.matchFit.user.entity.User
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne

@Entity
class Participation() : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false)
    lateinit var user: User

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "postId", nullable = false)
    lateinit var post: Post

    @Enumerated(EnumType.STRING)
    lateinit var status: ApplicationStatus

    constructor(user: User, post: Post) : this() {
        this.user = user
        this.post = post
        this.status = ApplicationStatus.PENDING
    }
}
