package com.matchFit.post.dto.response

data class GetMyPosts(
    val posts: List<GetMyPost>
) {
    companion object {
        fun from(myPosts: List<GetMyPost>): GetMyPosts {
            return GetMyPosts(myPosts)
        }
    }
}
