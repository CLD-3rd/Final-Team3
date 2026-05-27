package com.matchFit.post.dto;

import com.matchFit.post.entity.Sports;
import com.matchFit.post.entity.Town;
import com.matchFit.user.entity.Gender;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePostRequestDto {
    private String title;
    private String description;
    private String location;
    private LocalDateTime date;
    private Integer maxPeople;
    private Gender gender;
    private Integer cost;
    private String imageUrl;
    private Sports sports;
    private Town town;
    private Boolean removeImage;
}
