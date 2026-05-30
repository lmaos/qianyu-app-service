package com.clmcat.qianyu.mall.mch.service;

import com.clmcat.qianyu.mall.mch.model.dto.FreightRuleCreateDTO;
import com.clmcat.qianyu.mall.mch.model.dto.FreightTemplateCreateDTO;
import com.clmcat.qianyu.mall.mch.model.dto.FreightTemplateUpdateDTO;
import com.clmcat.qianyu.mall.mch.model.entity.Merchant;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantFreightRule;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantFreightTemplate;
import com.clmcat.qianyu.mall.mch.model.entity.status.MchStatus;
import com.clmcat.qianyu.mall.mch.model.vo.FreightRuleVO;
import com.clmcat.qianyu.mall.mch.model.vo.FreightTemplateDetailVO;
import com.clmcat.qianyu.mall.mch.model.vo.FreightTemplateVO;
import com.clmcat.qianyu.mall.mch.support.MerchantConvert;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class MerchantFreightViewServiceBiz {

    @Resource
    private MerchantServiceBiz merchantServiceBiz;

    @Resource
    private MerchantFreightServiceBiz freightServiceBiz;

    /**
     * 模板列表
     */
    public List<FreightTemplateVO> getTemplateList(long userId) {
        Merchant merchant = merchantServiceBiz.selectByUserId(userId);
        MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(merchant == null);

        List<MerchantFreightTemplate> templates = freightServiceBiz.selectTemplatesByMerchantId(merchant.getId());
        List<FreightTemplateVO> voList = new ArrayList<>();
        if (templates != null) {
            for (MerchantFreightTemplate t : templates) {
                List<MerchantFreightRule> rules = freightServiceBiz.selectRulesByTemplateId(t.getId());
                int ruleCount = rules != null ? rules.size() : 0;
                voList.add(MerchantConvert.toFreightTemplateVO(t, ruleCount));
            }
        }
        return voList;
    }

    /**
     * 模板详情
     */
    public FreightTemplateDetailVO getTemplateDetail(long userId, Long templateId) {
        Merchant merchant = merchantServiceBiz.selectByUserId(userId);
        MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(merchant == null);

        MchStatus.MCH_FREIGHT_TEMPLATE_NOT_FOUND.assertThrowResEx(MerchantConvert.isNullOrNonPositive(templateId));
        MerchantFreightTemplate template = freightServiceBiz.selectTemplateOneById(templateId);
        MchStatus.MCH_FREIGHT_TEMPLATE_NOT_FOUND.assertThrowResEx(template == null);

        // 校验归属
        MchStatus.MCH_FREIGHT_TEMPLATE_NOT_FOUND.assertThrowResEx(!template.getMerchantId().equals(merchant.getId()));

        List<MerchantFreightRule> rules = freightServiceBiz.selectRulesByTemplateId(templateId);
        List<FreightRuleVO> ruleVOs = MerchantConvert.toFreightRuleVOList(rules);

        return FreightTemplateDetailVO.builder()
                .id(template.getId())
                .name(template.getName())
                .billingType(template.getBillingType())
                .freeShippingType(template.getFreeShippingType())
                .freeShippingValue(template.getFreeShippingValue() != null
                        ? template.getFreeShippingValue().toPlainString() : null)
                .isDefault(template.getIsDefault() != null && template.getIsDefault() == 1)
                .status(template.getStatus())
                .rules(ruleVOs)
                .build();
    }

    /**
     * 创建模板
     */
    public Long createTemplate(long userId, FreightTemplateCreateDTO dto) {
        Merchant merchant = merchantServiceBiz.selectByUserId(userId);
        MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(merchant == null);

        long now = System.currentTimeMillis();

        // 插入模板
        MerchantFreightTemplate template = new MerchantFreightTemplate();
        long templateId = MerchantConvert.FREIGHT_TEMPLATE_ID_SNOWFLAKE.nextId();
        template.setId(templateId);
        template.setMerchantId(merchant.getId());
        template.setName(dto.getName());
        template.setBillingType(dto.getBillingType() != null ? dto.getBillingType() : 1);
        template.setFreeShippingType(dto.getFreeShippingType() != null ? dto.getFreeShippingType() : 0);
        template.setFreeShippingValue(dto.getFreeShippingValue() != null ? new BigDecimal(dto.getFreeShippingValue()) : BigDecimal.ZERO);
        template.setIsDefault(dto.getIsDefault() != null && dto.getIsDefault() ? 1 : 0);
        template.setStatus(1); // 启用
        template.setCreateTime(now);
        template.setUpdateTime(now);
        template.setDeleted(0);
        freightServiceBiz.insertTemplate(template);

        // 批量插入规则
        if (dto.getRules() != null) {
            for (FreightRuleCreateDTO ruleDTO : dto.getRules()) {
                MerchantFreightRule rule = new MerchantFreightRule();
                rule.setId(MerchantConvert.FREIGHT_RULE_ID_SNOWFLAKE.nextId());
                rule.setTemplateId(templateId);
                rule.setDestinationType(ruleDTO.getDestinationType());
                rule.setDestination(ruleDTO.getDestination());
                rule.setFirstUnit(ruleDTO.getFirstUnit());
                rule.setFirstPrice(ruleDTO.getFirstPrice() != null ? new BigDecimal(ruleDTO.getFirstPrice()) : null);
                rule.setAdditionalUnit(ruleDTO.getAdditionalUnit());
                rule.setAdditionalPrice(ruleDTO.getAdditionalPrice() != null ? new BigDecimal(ruleDTO.getAdditionalPrice()) : null);
                rule.setCreateTime(now);
                rule.setUpdateTime(now);
                freightServiceBiz.insertRule(rule);
            }
        }

        return templateId;
    }

    /**
     * 更新模板
     */
    public void updateTemplate(long userId, FreightTemplateUpdateDTO dto) {
        Merchant merchant = merchantServiceBiz.selectByUserId(userId);
        MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(merchant == null);

        MchStatus.MCH_FREIGHT_TEMPLATE_NOT_FOUND.assertThrowResEx(MerchantConvert.isNullOrNonPositive(dto.getTemplateId()));
        MerchantFreightTemplate template = freightServiceBiz.selectTemplateOneById(dto.getTemplateId());
        MchStatus.MCH_FREIGHT_TEMPLATE_NOT_FOUND.assertThrowResEx(template == null);

        // 校验归属
        MchStatus.MCH_FREIGHT_TEMPLATE_NOT_FOUND.assertThrowResEx(!template.getMerchantId().equals(merchant.getId()));

        long now = System.currentTimeMillis();

        // 更新模板字段
        template.setName(dto.getName());
        template.setBillingType(dto.getBillingType());
        template.setFreeShippingType(dto.getFreeShippingType() != null ? dto.getFreeShippingType() : 0);
        template.setFreeShippingValue(dto.getFreeShippingValue() != null ? new BigDecimal(dto.getFreeShippingValue()) : null);
        template.setIsDefault(dto.getIsDefault() != null && dto.getIsDefault() ? 1 : 0);
        template.setUpdateTime(now);
        freightServiceBiz.updateTemplate(template);

        // 删除旧规则
        List<MerchantFreightRule> oldRules = freightServiceBiz.selectRulesByTemplateId(dto.getTemplateId());
        if (oldRules != null) {
            for (MerchantFreightRule oldRule : oldRules) {
                freightServiceBiz.deleteRuleById(oldRule.getId());
            }
        }

        // 重新插入新规则
        if (dto.getRules() != null) {
            for (FreightRuleCreateDTO ruleDTO : dto.getRules()) {
                MerchantFreightRule rule = new MerchantFreightRule();
                rule.setId(MerchantConvert.FREIGHT_RULE_ID_SNOWFLAKE.nextId());
                rule.setTemplateId(dto.getTemplateId());
                rule.setDestinationType(ruleDTO.getDestinationType());
                rule.setDestination(ruleDTO.getDestination());
                rule.setFirstUnit(ruleDTO.getFirstUnit());
                rule.setFirstPrice(ruleDTO.getFirstPrice() != null ? new BigDecimal(ruleDTO.getFirstPrice()) : null);
                rule.setAdditionalUnit(ruleDTO.getAdditionalUnit());
                rule.setAdditionalPrice(ruleDTO.getAdditionalPrice() != null ? new BigDecimal(ruleDTO.getAdditionalPrice()) : null);
                rule.setCreateTime(now);
                rule.setUpdateTime(now);
                freightServiceBiz.insertRule(rule);
            }
        }
    }

    /**
     * 删除模板
     */
    public void deleteTemplate(long userId, Long templateId) {
        Merchant merchant = merchantServiceBiz.selectByUserId(userId);
        MchStatus.MCH_NOT_MERCHANT.assertThrowResEx(merchant == null);

        MchStatus.MCH_FREIGHT_TEMPLATE_NOT_FOUND.assertThrowResEx(MerchantConvert.isNullOrNonPositive(templateId));
        MerchantFreightTemplate template = freightServiceBiz.selectTemplateOneById(templateId);
        MchStatus.MCH_FREIGHT_TEMPLATE_NOT_FOUND.assertThrowResEx(template == null);

        // 校验归属
        MchStatus.MCH_FREIGHT_TEMPLATE_NOT_FOUND.assertThrowResEx(!template.getMerchantId().equals(merchant.getId()));

        // TODO: 替换真实接口 - 校验是否被商品引用
        // MchStatus.MCH_FREIGHT_TEMPLATE_IN_USE.assertThrowResEx(inUse);

        // 逻辑删除
        freightServiceBiz.deleteTemplateById(templateId);
    }
}
