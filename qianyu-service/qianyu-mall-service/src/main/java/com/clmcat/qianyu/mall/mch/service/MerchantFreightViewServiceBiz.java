package com.clmcat.qianyu.mall.mch.service;

import com.clmcat.qianyu.mall.mch.model.dto.FreightTemplateCreateDTO;
import com.clmcat.qianyu.mall.mch.model.dto.FreightTemplateUpdateDTO;
import com.clmcat.qianyu.mall.mch.model.vo.FreightTemplateDetailVO;
import com.clmcat.qianyu.mall.mch.model.vo.FreightTemplateVO;
import java.util.List;

public interface MerchantFreightViewServiceBiz {

    List<FreightTemplateVO> getTemplateList(long userId);

    FreightTemplateDetailVO getTemplateDetail(long userId, Long templateId);

    Long createTemplate(long userId, FreightTemplateCreateDTO dto);

    void updateTemplate(long userId, FreightTemplateUpdateDTO dto);

    void deleteTemplate(long userId, Long templateId);

}