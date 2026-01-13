package com.matchFit.user.dto.request

import com.matchFit.post.entity.Sports


class EditMyPageRequest {
    lateinit var nickName: String
    lateinit var sports: Sports
    var age: Int = 0
}
