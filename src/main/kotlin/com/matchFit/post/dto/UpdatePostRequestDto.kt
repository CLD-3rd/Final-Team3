package com.matchFit.post.dto

import com.matchFit.post.entity.Sports
import com.matchFit.post.entity.Town
import com.matchFit.user.entity.Gender
import java.time.LocalDateTime

class UpdatePostRequestDto {
    var title: String? = null
    var description: String? = null
    var location: String? = null
    var date: LocalDateTime? = null
    var maxPeople: Int? = null
    var gender: Gender? = null
    var cost: Int? = null
    var imageUrl: String? = null
    var sports: Sports? = null
    var town: Town? = null
    var removeImage: Boolean? = null
}
