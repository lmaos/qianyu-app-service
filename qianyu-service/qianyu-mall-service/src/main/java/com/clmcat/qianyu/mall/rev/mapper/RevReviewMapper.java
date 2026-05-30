package com.clmcat.qianyu.mall.rev.mapper;

import com.clmcat.qianyu.mall.rev.model.entity.RevReview;
import com.mybatisflex.core.BaseMapper;
import com.mybatisflex.core.paginate.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface RevReviewMapper extends BaseMapper<RevReview> {

    /**
     * 检查订单商品是否已评价
     */
    @Select("SELECT COUNT(*) FROM rev_review " +
            "WHERE order_item_id = #{orderItemId} AND deleted = 0")
    int countByOrderItemId(@Param("orderItemId") Long orderItemId);

    /**
     * 商品评价列表（分页，仅 status=1 正常评价）
     */
    @Select("<script>" +
            "SELECT * FROM rev_review WHERE spu_id = #{spuId} AND status = 1 AND deleted = 0 " +
            "<if test='score == 1'> AND score &lt;= 2 </if> " +
            "<if test='score == 2'> AND score = 3 </if> " +
            "<if test='score == 3'> AND score &gt;= 4 </if> " +
            "<if test='score == 4'> AND images IS NOT NULL AND JSON_LENGTH(images) > 0 </if> " +
            "ORDER BY " +
            "<choose><when test='sortField == \"score\"'>score DESC, create_time DESC</when>" +
            "<otherwise>create_time DESC</otherwise></choose>" +
            "</script>")
    Page<RevReview> selectBySpuId(Page<RevReview> page,
                                    @Param("spuId") Long spuId,
                                    @Param("score") Integer score,
                                    @Param("sortField") String sortField);

    /**
     * 商家评价列表
     */
    @Select("<script>" +
            "SELECT * FROM rev_review WHERE merchant_id = #{merchantId} AND deleted = 0 " +
            "<if test='spuId != null'> AND spu_id = #{spuId} </if> " +
            "<if test='score != null'> AND score = #{score} </if> " +
            "<if test='hasReply != null and hasReply'> AND reply_content IS NOT NULL </if> " +
            "<if test='hasReply != null and !hasReply'> AND reply_content IS NULL </if> " +
            "ORDER BY create_time DESC" +
            "</script>")
    Page<RevReview> selectByMerchantId(Page<RevReview> page,
                                         @Param("merchantId") Long merchantId,
                                         @Param("spuId") Long spuId,
                                         @Param("score") Integer score,
                                         @Param("hasReply") Boolean hasReply);
}
