package com.clmcat.qianyu.user.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Token;
import org.springframework.web.bind.annotation.RequestMapping;

@ApiController
@RequestMapping("/api/user")
@LoginVerify
public class UserController {

    @RequestMapping("/value")
    public String value(@Token long userId) {
        return "user:" + userId;
    }


}
