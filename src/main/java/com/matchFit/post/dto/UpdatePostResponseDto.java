package com.matchFit.post.dto;

import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Sports;
import com.matchFit.post.entity.Status;
import com.matchFit.post.entity.Town;
import com.matchFit.user.entity.Gender;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UpdatePostResponseDto {
    private final String title;
    private final String description;
    private final String location;
    private final LocalDateTime date;
    private final int maxPeople;
    private final Gender gender;
    private final Status status;
    private final int cost;
    private final String imageUrl;
    private final Sports sports;
    private final Town town;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

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
