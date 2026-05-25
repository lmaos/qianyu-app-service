package com.clmcat.qianyu.social.api.follow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowCountDto {
    private Long userId;
    private Long followCount;
    private Long followerCount;
}
