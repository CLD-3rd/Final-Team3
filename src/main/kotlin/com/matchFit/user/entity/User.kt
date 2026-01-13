package com.matchFit.user.entity

import com.matchFit.common.entity.BaseEntity
import com.matchFit.post.entity.Sports
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity
class User : BaseEntity() {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @Column(nullable = false, unique = true)
    lateinit var email: String

    @Column(nullable = false)
    lateinit var password: String

    @Column(nullable = false, unique = true)
    lateinit var nickname: String

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    lateinit var gender: Gender

    @Column(nullable = false)
    var age: Int = 0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    lateinit var sports: Sports

    @Column(nullable = false)
    lateinit var town: String

    @Column(nullable = false)
    var recruitCount: Int = 0

    @Column(nullable = false)
    var joinCount: Int = 0
}
