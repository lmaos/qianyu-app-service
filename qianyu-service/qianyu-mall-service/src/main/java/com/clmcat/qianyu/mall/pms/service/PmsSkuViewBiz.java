package com.clmcat.qianyu.mall.pms.service;

import com.clmcat.qianyu.mall.pms.model.vo.SkuItemVo;
import java.util.List;

public interface PmsSkuViewBiz {

    List<SkuItemVo> getSkuList(Long spuId);

}