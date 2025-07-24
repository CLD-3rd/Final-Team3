package com.matchFit.post.dto;

import java.time.LocalDateTime;

import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Sports;
import com.matchFit.post.entity.Status;
import com.matchFit.post.entity.Town;
import com.matchFit.user.entity.Gender;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdatePostResponseDto {
    private String title;
    private String description;
    private String location;
    private LocalDateTime date;
    private Integer maxPeople;
    private Gender gender;
    private Status status;
    private Integer cost;
    private String imageUrl;
    private Sports sports;
    private Town town;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static UpdatePostResponseDto from(Post post) {
        return new UpdatePostResponseDto(
            post.getTitle(),
            post.getDescription(),
            post.getLocation(),
            post.getDate(),
            post.getMaxPeople(),
            post.getGender(),
            post.getStatus(),
            post.getCost(),
            post.getImageUrl(),
            post.getSports(),
            post.getTown(),
            post.getCreatedAt(),
            post.getUpdatedAt()
        );
    }
}