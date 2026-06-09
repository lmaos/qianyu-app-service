package com.clmcat.qianyu.social.api.moment.model.dto;

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
public class MomentListDto implements Serializable {
    public static final MomentListDto EMPTY = MomentListDto.builder().moments(new ArrayList<>()).build();

    @Serial
    private static final long serialVersionUID = 1L;
    private List<MomentDto> moments;
    private Long last;
}
