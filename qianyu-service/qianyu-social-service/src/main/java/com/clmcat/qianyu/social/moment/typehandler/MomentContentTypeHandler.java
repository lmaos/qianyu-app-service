package com.clmcat.qianyu.social.moment.typehandler;

import com.alibaba.fastjson2.JSON;

import com.clmcat.qianyu.social.api.moment.model.dto.MomentContent;
import com.mybatisflex.core.handler.BaseJsonTypeHandler;

public class MomentContentTypeHandler extends BaseJsonTypeHandler<MomentContent> {


    @Override
    public MomentContent parseJson(String json) {
        return JSON.parseObject(json, MomentContent.class);
    }


    @Override
    public String toJson(MomentContent obj) {
        return JSON.toJSONString(obj);
    }
}
