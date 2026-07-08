package com.clmcat.qianyu.search.usersearch.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户搜索主表实体，存当前昵称，服务精确匹配和前缀匹配。
 *
 * @author ark-home
 * @date 2026-07-07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("user_search")
public class UserSearch {

    /** 用户ID（业务主键） */
    @Id(keyType = KeyType.None)
    private Long userId;

    /** 当前昵称（唯一） */
    private String nickname;

    /** 更新时间戳（Unix 毫秒） */
    private Long updatedAt;
}
