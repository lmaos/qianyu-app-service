package com.clmcat.qianyu.social.api.moment.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class MomentIdListDto implements java.io.Serializable {
    public static final MomentIdListDto EMPTY = MomentIdListDto.builder().momentIds(new ArrayList<>()).build();

    @Serial
    private static final long serialVersionUID = 1L;

    private List<Long> momentIds;
}
