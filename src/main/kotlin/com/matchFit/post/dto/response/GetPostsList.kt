package com.matchFit.post.dto.response


data class GetPostsList(
    val posts: List<GetPost>,
    val page: Int = 0,
    val size: Int = posts.size,
    val totalElements: Long = posts.size.toLong(),
    val totalPages: Int = 1
) {
    companion object {
        fun of(posts: List<GetPost>): GetPostsList {
            return GetPostsList(posts)
        }

        fun of(
            posts: List<GetPost>,
            page: Int,
            size: Int,
            totalElements: Long,
            totalPages: Int
        ): GetPostsList {
            return GetPostsList(posts, page, size, totalElements, totalPages)
        }
    }
}
