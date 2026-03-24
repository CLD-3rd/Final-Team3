package com.matchFit.post.dto.response


data class GetPostsCalender(
    val posts: List<GetPostCalender>
) {
    companion object {
        fun from(postsCalender: List<GetPostCalender>): GetPostsCalender {
            return GetPostsCalender(postsCalender)
        }
    }
}
