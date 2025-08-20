package com.matchFit.post.dto;

import java.time.LocalDateTime;

import com.matchFit.post.entity.Sports;
import com.matchFit.post.entity.Status;
import com.matchFit.post.entity.Town;
import com.matchFit.user.entity.Gender;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
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