package com.clmcat.qianyu.mall.mch.rpc;

import com.clmcat.qianyu.mall.api.mch.MerchantFreightApi;
import com.clmcat.qianyu.mall.mch.mapper.MerchantFreightRuleMapper;
import com.clmcat.qianyu.mall.mch.mapper.MerchantFreightTemplateMapper;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantFreightRule;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantFreightTemplate;
import jakarta.annotation.Resource;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.stereotype.Service;

import java.util.List;

@DubboService
@Service
public class MerchantFreightApiImpl implements MerchantFreightApi {

    @Resource
    private MerchantFreightTemplateMapper freightTemplateMapper;

    @Resource
    private MerchantFreightRuleMapper freightRuleMapper;

    @Override
    public Object getById(Long templateId) {
        MerchantFreightTemplate template = freightTemplateMapper.selectOneById(templateId);
        if (template == null) return null;
        return template;
    }

    // ==================== Internal methods for ViewBiz ====================

    public List<MerchantFreightTemplate> selectTemplatesByMerchantId(Long merchantId) {
        return freightTemplateMapper.selectByMerchantId(merchantId);
    }

    public MerchantFreightTemplate selectTemplateOneById(Long id) {
        return freightTemplateMapper.selectOneById(id);
    }

    public void insertTemplate(MerchantFreightTemplate template) {
        freightTemplateMapper.insertSelective(template);
    }

    public void updateTemplate(MerchantFreightTemplate template) {
        freightTemplateMapper.update(template);
    }

    public void deleteTemplateById(Long id) {
        freightTemplateMapper.deleteById(id);
    }

    public List<MerchantFreightRule> selectRulesByTemplateId(Long templateId) {
        return freightRuleMapper.selectByTemplateId(templateId);
    }

    public void insertRule(MerchantFreightRule rule) {
        freightRuleMapper.insertSelective(rule);
    }

    public void deleteRuleById(Long id) {
        freightRuleMapper.deleteById(id);
    }
}
