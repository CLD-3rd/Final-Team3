package com.matchFit.user.dto.response

import com.matchFit.post.entity.Sports


data class MyPageResponse(
    val email: String,
    val nickName: String,
    val town: String,
    val age: Int,
    val sports: Sports
)
