package com.clmcat.qianyu.mall.pms.support;

import com.clmcat.basics.commons.snowflake.CustomSnowflake;
import com.clmcat.qianyu.core.snowflake.SnowflakeSupport;
import com.clmcat.qianyu.mall.pms.mapper.PmsAttributeMapper;
import com.clmcat.qianyu.mall.pms.mapper.PmsSkuMapper;
import com.clmcat.qianyu.mall.pms.mapper.PmsSpuMapper;
import com.clmcat.qianyu.mall.pms.model.entity.PmsAttribute;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSku;
import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.clmcat.qianyu.mall.pms.model.vo.SkuItemVo;
import com.clmcat.qianyu.mall.pms.model.vo.SpecGroupVo;
import com.clmcat.qianyu.mall.pms.model.vo.SpuSimpleVo;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Component
public class PmsSupport {

    public static final CustomSnowflake SNOWFLAKE = SnowflakeSupport.createSnowflake(42, 10, 11);

    @Resource
    private PmsSkuMapper skuMapper;

    @Resource
    private PmsSpuMapper spuMapper;

    @Resource
    private PmsAttributeMapper attributeMapper;

    /**
     * specs 字符串 -> attributes JSON 转换
     * 输入: "红色,XL"（前端传入的规格值拼接）
     * 查询 pms_attribute 获取对应的规格名（如 颜色、尺码）
     * 输出: [{"k":"颜色","v":"红色"},{"k":"尺码","v":"XL"}]
     *
     * @param specs      规格值拼接字符串，逗号分隔
     * @param categoryId 分类 ID，用于查询属性定义
     * @return attributes JSON 列表
     */
    public List<LinkedHashMap<String, String>> specsToAttributes(String specs, Long categoryId) {
        List<LinkedHashMap<String, String>> result = new ArrayList<>();
        if (specs == null || specs.isEmpty()) {
            return result;
        }

        // 查询该分类下的销售属性定义（按 sort 排序）
        List<PmsAttribute> saleAttrs = attributeMapper.selectSaleAttrByCategoryId(categoryId);
        String[] specValues = specs.split(",");

        for (int i = 0; i < specValues.length && i < saleAttrs.size(); i++) {
            LinkedHashMap<String, String> kv = new LinkedHashMap<>();
            kv.put("k", saleAttrs.get(i).getName());
            kv.put("v", specValues[i].trim());
            result.add(kv);
        }

        return result;
    }

    /**
     * attributes JSON -> specs 字符串转换
     * 输入: [{"k":"颜色","v":"红色"},{"k":"尺码","v":"XL"}]
     * 输出: "红色,XL"
     *
     * @param attributes SKU 属性 JSON 列表
     * @return 规格值拼接字符串
     */
    public String attributesToSpecs(List<LinkedHashMap<String, String>> attributes) {
        if (attributes == null || attributes.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < attributes.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(attributes.get(i).get("v"));
        }
        return sb.toString();
    }

    /**
     * 从 SKU 列表构建规格组 SpecGroupVo
     * 遍历所有 SKU 的 attributes，提取去重的 key 和对应 values
     * 例如: "颜色" -> ["红色","蓝色"], "尺码" -> ["S","M","L"]
     *
     * @param skuList SKU 列表
     * @return 规格组列表
     */
    public List<SpecGroupVo> buildSpecGroups(List<PmsSku> skuList) {
        // 使用 LinkedHashMap 保持插入顺序
        LinkedHashMap<String, LinkedHashSet<String>> specMap = new LinkedHashMap<>();

        for (PmsSku sku : skuList) {
            List<LinkedHashMap<String, String>> attributes = sku.getAttributes();
            if (attributes == null) {
                continue;
            }
            for (LinkedHashMap<String, String> attr : attributes) {
                String key = attr.get("k");
                String value = attr.get("v");
                if (key != null && value != null) {
                    specMap.computeIfAbsent(key, k -> new LinkedHashSet<>()).add(value);
                }
            }
        }

        List<SpecGroupVo> specGroups = new ArrayList<>();
        for (Map.Entry<String, LinkedHashSet<String>> entry : specMap.entrySet()) {
            specGroups.add(SpecGroupVo.builder()
                    .name(entry.getKey())
                    .values(new ArrayList<>(entry.getValue()))
                    .build());
        }

        return specGroups;
    }

    /**
     * 计算并更新 SPU 的 minPrice 冗余字段
     * 取所有未删除 SKU 中最低 price，更新 pms_spu.min_price
     *
     * @param spuId SPU ID
     */
    public void refreshMinPrice(Long spuId) {
        List<PmsSku> skuList = skuMapper.selectBySpuId(spuId);
        BigDecimal minPrice = skuList.stream()
                .map(PmsSku::getPrice)
                .filter(Objects::nonNull)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
        PmsSpu update = new PmsSpu();
        update.setId(spuId);
        update.setMinPrice(minPrice);
        update.setUpdateTime(System.currentTimeMillis());
        spuMapper.update(update);
    }

    /**
     * 获取 SPU 的默认 SKU 的 originalPrice
     * 用于列表页展示原价（划线价）
     *
     * @param spuId SPU ID
     * @return 默认 SKU 原价，无默认 SKU 则返回 null
     */
    public BigDecimal getDefaultSkuOriginalPrice(Long spuId) {
        PmsSku defaultSku = skuMapper.selectDefaultBySpuId(spuId);
        return defaultSku != null ? defaultSku.getOriginalPrice() : null;
    }

    /**
     * BigDecimal -> String（元）序列化
     * 去除末尾多余的零，如 199.000000 -> "199.00"
     *
     * @param amount 金额
     * @return 字符串金额（元）
     */
    public String amountToString(BigDecimal amount) {
        if (amount == null) {
            return null;
        }
        return amount.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    /**
     * 生成下一个 Snowflake ID
     */
    public long nextId() {
        return SNOWFLAKE.nextId();
    }

    /**
     * 解析 Snowflake ID 中的时间戳
     */
    public long parseTime(long snowflakeId) {
        return SnowflakeSupport.parseTimeBySnowflake(SNOWFLAKE, snowflakeId);
    }

    /**
     * PmsSku -> SkuItemVo 转换
     *
     * @param sku          SKU 实体
     * @param spuMainImage SPU 主图（SKU 无图时降级使用）
     * @param stock        可用库存
     * @return SkuItemVo
     */
    public SkuItemVo toSkuItemVo(PmsSku sku, String spuMainImage, Integer stock) {
        String specs = attributesToSpecs(sku.getAttributes());
        String image = sku.getSkuImage() != null ? sku.getSkuImage() : spuMainImage;

        return SkuItemVo.builder()
                .id(sku.getId())
                .skuName(sku.getSkuName())
                .specs(specs)
                .price(amountToString(sku.getPrice()))
                .originalPrice(amountToString(sku.getOriginalPrice()))
                .stock(stock)
                .image(image)
                .isDefault(sku.getIsDefault() != null && sku.getIsDefault() == 1)
                .merchantId(sku.getMerchantId())
                .weight(sku.getWeight())
                .volume(sku.getVolume())
                .build();
    }

    /**
     * PmsSpu -> SpuSimpleVo 转换
     *
     * @param spu SPU 实体
     * @return SpuSimpleVo
     */
    public SpuSimpleVo toSpuSimpleVo(PmsSpu spu) {
        BigDecimal originalPrice = getDefaultSkuOriginalPrice(spu.getId());

        return SpuSimpleVo.builder()
                .id(spu.getId())
                .name(spu.getName())
                .mainImage(spu.getThumbImage() != null ? spu.getThumbImage() : spu.getMainImage())
                .price(amountToString(spu.getMinPrice()))
                .originalPrice(amountToString(originalPrice))
                .sales(spu.getSales() != null ? spu.getSales() : 0)
                .commentCount(spu.getCommentCount() != null ? spu.getCommentCount() : 0)
                .avgScore(spu.getAvgScore())
                .merchantId(spu.getMerchantId())
                .merchantName(spu.getMerchantName())
                .storeId(spu.getStoreId())
                .storeName(spu.getStoreName())
                .build();
    }
}
