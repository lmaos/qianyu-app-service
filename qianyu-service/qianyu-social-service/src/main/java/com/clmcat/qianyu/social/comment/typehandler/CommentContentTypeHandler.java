package com.clmcat.qianyu.social.comment.typehandler;

import com.alibaba.fastjson2.JSON;
import com.clmcat.qianyu.social.api.comment.model.dto.CommentContent;
import com.mybatisflex.core.handler.BaseJsonTypeHandler;

public class CommentContentTypeHandler extends BaseJsonTypeHandler<CommentContent> {
    @Override
    public CommentContent parseJson(String json) {
        return JSON.parseObject(json, CommentContent.class);
    }

    @Override
    public String toJson(CommentContent obj) {
        return JSON.toJSONString(obj);
    }
}
