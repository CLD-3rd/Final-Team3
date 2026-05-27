package com.matchFit.post.dto;

import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Sports;
import com.matchFit.post.entity.Status;
import com.matchFit.post.entity.Town;
import com.matchFit.user.entity.Gender;
import java.time.LocalDateTime;
import lombok.Getter;

@Getter
public class PostInfoResponseDto {
    private final Long id;
    private final String title;
    private final String description;
    private final String imageUrl;
    private final Gender gender;
    private final Sports sports;
    private final int cost;
    private final Status status;
    private final Town town;
    private final int maxPeople;
    private final LocalDateTime date;
    private final String location;
    private final int currentPeople;
    private final boolean isBookmarked;
    private final String userEmail;

    public PostInfoResponseDto(Post post, int currentPeople, boolean isBookmarked) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.description = post.getDescription();
        this.imageUrl = post.getImageUrl();
        this.gender = post.getGender();
        this.sports = post.getSports();
        this.cost = post.getCost();
        this.status = post.getStatus();
        this.town = post.getTown();
        this.maxPeople = post.getMaxPeople();
        this.date = post.getDate();
        this.location = post.getLocation();
        this.currentPeople = currentPeople;
        this.isBookmarked = isBookmarked;
        this.userEmail = post.getUser().getEmail();
    }
}
