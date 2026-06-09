package com.clmcat.qianyu.social.api.follow.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class FollowListDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final FollowListDto EMPTY = FollowListDto.builder().follows(new ArrayList<>()).build();

    private List<FollowDto> follows;
}
