package com.clmcat.qianyu.mall.pms.mapper;

import com.clmcat.qianyu.mall.pms.model.entity.PmsSpu;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.math.BigDecimal;

@Mapper
public interface PmsSpuMapper extends BaseMapper<PmsSpu> {

    /**
     * SPU 搜索（支持关键词/分类/品牌/价格区间/排序/分页）
     */
    @Select("<script>" +
            "SELECT s.*, m.name AS merchant_name, st.name AS store_name " +
            "FROM pms_spu s " +
            "LEFT JOIN mch_merchant m ON s.merchant_id = m.id " +
            "LEFT JOIN mch_store st ON s.store_id = st.id " +
            "WHERE s.status = 1 AND s.deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND (s.name LIKE CONCAT('%', #{keyword}, '%') OR s.keywords LIKE CONCAT('%', #{keyword}, '%'))" +
            "</if>" +
            "<if test='categoryId != null'>" +
            "  AND s.category_id = #{categoryId}" +
            "</if>" +
            "<if test='brandId != null'>" +
            "  AND s.brand_id = #{brandId}" +
            "</if>" +
            "<if test='minPrice != null'>" +
            "  AND s.min_price &gt;= #{minPrice}" +
            "</if>" +
            "<if test='maxPrice != null'>" +
            "  AND s.min_price &lt;= #{maxPrice}" +
            "</if>" +
            "ORDER BY " +
            "<choose>" +
            "  <when test='sortField == \"price\"'>s.min_price ${sortOrder}</when>" +
            "  <when test='sortField == \"sales\"'>s.sales ${sortOrder}</when>" +
            "  <otherwise>s.create_time ${sortOrder}</otherwise>" +
            "</choose>" +
            "</script>")
    Page<PmsSpu> searchSpu(Page<PmsSpu> page,
                             @Param("keyword") String keyword,
                             @Param("categoryId") Long categoryId,
                             @Param("brandId") Long brandId,
                             @Param("minPrice") BigDecimal minPrice,
                             @Param("maxPrice") BigDecimal maxPrice,
                             @Param("sortField") String sortField,
                             @Param("sortOrder") String sortOrder);

    /**
     * SPU 列表（按分类/商家，仅上架状态）
     */
    @Select("<script>" +
            "SELECT s.*, m.name AS merchant_name, st.name AS store_name " +
            "FROM pms_spu s " +
            "LEFT JOIN mch_merchant m ON s.merchant_id = m.id " +
            "LEFT JOIN mch_store st ON s.store_id = st.id " +
            "WHERE s.status = 1 AND s.deleted = 0 " +
            "<if test='categoryId != null'>" +
            "  AND s.category_id = #{categoryId}" +
            "</if>" +
            "<if test='merchantId != null'>" +
            "  AND s.merchant_id = #{merchantId}" +
            "</if>" +
            "ORDER BY s.sort ASC, s.create_time DESC" +
            "</script>")
    Page<PmsSpu> selectSpuList(Page<PmsSpu> page,
                                 @Param("categoryId") Long categoryId,
                                 @Param("merchantId") Long merchantId);
}
