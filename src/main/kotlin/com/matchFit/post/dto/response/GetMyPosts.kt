package com.matchFit.post.dto.response

data class GetMyPosts(
    val posts: List<GetMyPost>
) {
    companion object {
        fun of(myPosts: List<GetMyPost>) = GetMyPosts(myPosts)
    }
}
