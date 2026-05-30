package com.clmcat.qianyu.mall.pms.mapper;

import com.clmcat.qianyu.mall.pms.model.entity.PmsBrand;
import com.mybatisflex.core.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PmsBrandMapper extends BaseMapper<PmsBrand> {

    /**
     * 按分类 ID 筛选品牌（通过 pms_spu 关联，去重）
     */
    @Select("<script>" +
            "SELECT DISTINCT b.id, b.name, b.logo, b.description " +
            "FROM pms_brand b " +
            "INNER JOIN pms_spu s ON s.brand_id = b.id " +
            "INNER JOIN pms_spu_category sc ON sc.spu_id = s.id " +
            "WHERE b.status = 0 AND b.deleted = 0 AND s.deleted = 0 " +
            "<if test='categoryIds != null and categoryIds.size() > 0'>" +
            "  AND sc.category_id IN " +
            "  <foreach collection='categoryIds' item='cid' open='(' separator=',' close=')'>" +
            "    #{cid}" +
            "  </foreach>" +
            "</if>" +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND b.name LIKE CONCAT('%', #{keyword}, '%')" +
            "</if>" +
            "ORDER BY b.sort ASC" +
            "</script>")
    List<PmsBrand> selectByCategoryIdsOrKeyword(@Param("categoryIds") List<Long> categoryIds,
                                                  @Param("keyword") String keyword);

    /**
     * 按关键词搜索品牌
     */
    @Select("<script>" +
            "SELECT * FROM pms_brand WHERE status = 0 AND deleted = 0 " +
            "<if test='keyword != null and keyword != \"\"'>" +
            "  AND name LIKE CONCAT('%', #{keyword}, '%')" +
            "</if>" +
            "ORDER BY sort ASC" +
            "</script>")
    List<PmsBrand> selectByKeyword(@Param("keyword") String keyword);

    /**
     * 检查品牌名是否重复
     */
    @Select("SELECT COUNT(*) FROM pms_brand WHERE name = #{name} AND deleted = 0")
    int countByName(@Param("name") String name);
}
