package com.clmcat.qianyu.mall.api.pms;

import com.clmcat.qianyu.mall.api.model.dto.PageResultDTO;
import com.clmcat.qianyu.mall.api.pms.model.dto.PmsSpuDto;
import com.clmcat.qianyu.mall.api.pms.model.dto.SpuPageQueryDTO;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

public interface PmsSpuApi {

    PmsSpuDto getById(Long spuId);

    List<PmsSpuDto> batchGetByIds(Collection<Long> spuIds);

    void updateStatFields(Long spuId, BigDecimal minPrice, Integer sales, Integer commentCount, BigDecimal avgScore);

    /** 平台跨店商品分页（运营端）。 */
    PageResultDTO<PmsSpuDto> pageByPlatform(SpuPageQueryDTO query);

    /** 强制下架（status→2）。 */
    void adminListOff(Long spuId, String reason);

    /** 审核商品（approved=true→上架 1；false→保留草稿 0）。 */
    void audit(Long spuId, Boolean approved, String rejectReason);
}
