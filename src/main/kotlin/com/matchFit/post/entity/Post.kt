package com.matchFit.post.entity

import com.matchFit.common.entity.BaseEntity
import com.matchFit.user.entity.Gender
import com.matchFit.user.entity.User
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import java.time.LocalDateTime

@Entity
class Post : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false)
    lateinit var title: String

    @Column(nullable = false, length = 300)
    lateinit var description: String

    @Column(nullable = true)
    var imageUrl: String? = null

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    lateinit var gender: Gender

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    lateinit var sports: Sports

    @Column(nullable = false)
    var cost: Int = 0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    lateinit var status: Status

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    lateinit var town: Town

    @Column(nullable = false)
    var maxPeople: Int = 0

    @Column(nullable = false)
    lateinit var date: LocalDateTime

    @Column(nullable = false)
    lateinit var location: String

    @ManyToOne
    @JoinColumn(name = "user_id")
    lateinit var user: User
}
