use qianyu;
-- 用户搜索主表（精确/前缀匹配）
CREATE TABLE user_search (
                             user_id    BIGINT      PRIMARY KEY COMMENT '用户ID',
                             nickname   VARCHAR(64) NOT NULL COMMENT '当前昵称（唯一）',
                             updated_at BIGINT      NOT NULL COMMENT '更新时间戳(Unix毫秒)',
                             UNIQUE KEY uk_nickname (nickname)
);

-- NGram 倒排索引（模糊匹配）
CREATE TABLE user_search_ngram (
                                   id      BIGINT      PRIMARY KEY AUTO_INCREMENT,
                                   token   VARCHAR(12) NOT NULL COMMENT 'NGram token(n=2)',
                                   user_id BIGINT      NOT NULL,
                                   UNIQUE KEY uk_token_user (token, user_id),
                                   KEY idx_token (token)
);