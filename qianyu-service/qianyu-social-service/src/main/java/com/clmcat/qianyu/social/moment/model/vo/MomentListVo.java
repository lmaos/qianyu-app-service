package com.clmcat.qianyu.social.moment.model.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
public class MomentListVo {
    private List<MomentVo> datas;
}
