package com.matchFit.user.dto.request


class SignUpRequest {
    lateinit var email: String
    var password: String? = null
    lateinit var nickname: String
    var age: Int = 0
    lateinit var gender: String
    lateinit var town: String
    lateinit var sports: String
    var isKakaoUser: Boolean = false
}
