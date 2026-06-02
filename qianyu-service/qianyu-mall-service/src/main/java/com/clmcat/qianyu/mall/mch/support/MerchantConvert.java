package com.clmcat.qianyu.mall.mch.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantAccount;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantFreightRule;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantFreightTemplate;
import com.clmcat.qianyu.mall.mch.model.entity.MerchantWithdrawal;
import com.clmcat.qianyu.mall.mch.model.vo.AccountInfoVO;
import com.clmcat.qianyu.mall.mch.model.vo.FreightRuleVO;
import com.clmcat.qianyu.mall.mch.model.vo.FreightTemplateVO;
import com.clmcat.qianyu.mall.mch.model.vo.WithdrawItemVO;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MerchantConvert {

    public static final CustomSnowflake MERCHANT_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    public static final CustomSnowflake STORE_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    public static final CustomSnowflake ACCOUNT_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    public static final CustomSnowflake WITHDRAWAL_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    public static final CustomSnowflake FREIGHT_TEMPLATE_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);
    public static final CustomSnowflake FREIGHT_RULE_ID_SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static String formatTime(Long timestamp) {
        if (timestamp == null || timestamp == 0) return null;
        return DATE_FORMAT.format(new Date(timestamp));
    }

    public static String decimalToStr(BigDecimal value) {
        if (value == null) return "0.00";
        return value.toPlainString();
    }

    public static AccountInfoVO toAccountInfoVO(Long merchantId, MerchantAccount account) {
        if (account == null) return null;
        return AccountInfoVO.builder()
                .merchantId(merchantId)
                .balance(decimalToStr(account.getBalance()))
                .frozenAmount(decimalToStr(account.getFrozenAmount()))
                .totalIncome(decimalToStr(account.getTotalIncome()))
                .totalWithdraw(decimalToStr(account.getTotalWithdraw()))
                .totalRefund(decimalToStr(account.getTotalRefund()))
                .totalCommission(decimalToStr(account.getTotalCommission()))
                .build();
    }

    public static FreightTemplateVO toFreightTemplateVO(MerchantFreightTemplate template, int ruleCount) {
        if (template == null) return null;
        return FreightTemplateVO.builder()
                .id(template.getId())
                .name(template.getName())
                .billingType(template.getBillingType())
                .freeShippingType(template.getFreeShippingType())
                .freeShippingValue(template.getFreeShippingValue() != null
                        ? template.getFreeShippingValue().toPlainString() : null)
                .isDefault(template.getIsDefault() != null && template.getIsDefault() == 1)
                .status(template.getStatus())
                .ruleCount(ruleCount)
                .build();
    }

    public static FreightRuleVO toFreightRuleVO(MerchantFreightRule rule) {
        if (rule == null) return null;
        return FreightRuleVO.builder()
                .id(rule.getId())
                .destinationType(rule.getDestinationType())
                .destination(rule.getDestination())
                .firstUnit(rule.getFirstUnit())
                .firstPrice(rule.getFirstPrice() != null ? rule.getFirstPrice().toPlainString() : null)
                .additionalUnit(rule.getAdditionalUnit())
                .additionalPrice(rule.getAdditionalPrice() != null ? rule.getAdditionalPrice().toPlainString() : null)
                .build();
    }

    public static List<FreightRuleVO> toFreightRuleVOList(List<MerchantFreightRule> rules) {
        List<FreightRuleVO> list = new ArrayList<>();
        if (rules == null) return list;
        for (MerchantFreightRule rule : rules) {
            FreightRuleVO vo = toFreightRuleVO(rule);
            if (vo != null) list.add(vo);
        }
        return list;
    }

    public static WithdrawItemVO toWithdrawItemVO(MerchantWithdrawal withdrawal) {
        if (withdrawal == null) return null;
        return WithdrawItemVO.builder()
                .id(withdrawal.getId())
                .withdrawalNo(withdrawal.getWithdrawalNo())
                .amount(decimalToStr(withdrawal.getAmount()))
                .bankName(withdrawal.getBankName())
                .bankAccount(withdrawal.getBankAccount())
                .accountName(withdrawal.getAccountName())
                .status(withdrawal.getStatus())
                .statusText(getWithdrawStatusText(withdrawal.getStatus()))
                .rejectReason(withdrawal.getRejectReason())
                .createTime(formatTime(withdrawal.getCreateTime()))
                .build();
    }

    public static String getWithdrawStatusText(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "待审核";
            case 1: return "审核通过";
            case 2: return "打款中";
            case 3: return "打款成功";
            case 4: return "审核拒绝";
            case 5: return "打款失败";
            default: return "";
        }
    }

    public static String getBillTypeText(Integer type) {
        if (type == null) return "";
        switch (type) {
            case 1: return "订单收入";
            case 2: return "退款支出";
            case 3: return "佣金调整";
            default: return "";
        }
    }

    public static String getAuditStatusText(Integer auditStatus) {
        if (auditStatus == null) return "";
        switch (auditStatus) {
            case 0: return "待审核";
            case 1: return "审核通过";
            case 2: return "审核拒绝";
            default: return "";
        }
    }

    public static boolean isNullOrNonPositive(Number num) {
        return num == null || num.doubleValue() <= 0;
    }
}
