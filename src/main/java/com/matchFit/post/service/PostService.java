package com.matchFit.post.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.matchFit.post.dto.response.GetPost;
import com.matchFit.post.dto.response.GetPostsList;
import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Sports;
import com.matchFit.post.repository.PostRepository;
import com.matchFit.user.entity.Gender;

@Service
public class PostService {

    private final PostRepository postRepository;
    public PostService(PostRepository postRepository) {
        this.postRepository = postRepository;
    }

    public GetPostsList findByFilters(Sports sports, Gender gender, boolean nearest) {
        
        List<Post> posts = postRepository.findByFilters(
            sports != null ? sports.name() : null, 
            gender != null ? gender.name() : null, 
            nearest
        );
        
		List<GetPost> postDtos = GetPost.from(posts);

        return GetPostsList.of(postDtos);
    }
}
