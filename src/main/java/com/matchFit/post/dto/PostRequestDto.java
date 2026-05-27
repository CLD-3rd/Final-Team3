package com.matchFit.post.dto;

import com.matchFit.post.entity.Post;
import com.matchFit.post.entity.Sports;
import com.matchFit.post.entity.Status;
import com.matchFit.post.entity.Town;
import com.matchFit.user.entity.Gender;
import com.matchFit.user.entity.User;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PostRequestDto {
    private Long id;
    private String title;
    private String description;
    private String location;
    private String imageUrl;
    private Gender gender;
    private Sports sports;
    private Integer cost;
    private Status status;
    private Town town;
    private Integer maxPeople;
    private LocalDateTime date;

    public Post toEntity(User user) {
        Post post = new Post();
        post.setTitle(title == null ? "" : title);
        post.setDescription(description == null ? "" : description);
        post.setLocation(location == null ? "" : location);
        post.setImageUrl(imageUrl);
        if (gender == null) throw new IllegalArgumentException("gender is required");
        post.setGender(gender);
        if (sports == null) throw new IllegalArgumentException("sports is required");
        post.setSports(sports);
        post.setCost(cost == null ? 0 : cost);
        post.setStatus(status == null ? Status.OPEN : status);
        if (town == null) throw new IllegalArgumentException("town is required");
        post.setTown(town);
        post.setMaxPeople(maxPeople == null ? 0 : maxPeople);
        if (date == null) throw new IllegalArgumentException("date is required");
        post.setDate(date);
        post.setUser(user);
        return post;
    }
}
