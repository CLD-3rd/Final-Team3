package com.matchFit.post.dto.response


data class GetPostsCalender(
    val posts: List<GetPostCalender>
) {
    companion object {
        fun of(postsCalender: List<GetPostCalender>): GetPostsCalender =
            GetPostsCalender(postsCalender)
    }
}
