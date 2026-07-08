package com.clmcat.qianyu.search.usersearch.model.entity;

import com.mybatisflex.annotation.Id;
import com.mybatisflex.annotation.KeyType;
import com.mybatisflex.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * NGram 倒排索引实体，存储用户昵称的 bigram token。
 *
 * @author ark-home
 * @date 2026-07-07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table("user_search_ngram")
public class UserSearchNgram {

    @Id(keyType = KeyType.Auto)
    private Long id;

    /** NGram token（n=2） */
    private String token;

    /** 用户ID */
    private Long userId;
}
