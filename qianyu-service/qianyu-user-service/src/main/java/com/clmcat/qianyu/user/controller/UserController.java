package com.clmcat.qianyu.user.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import org.springframework.web.bind.annotation.RequestMapping;

@ApiController
@RequestMapping("/user")
public class UserController {

    @RequestMapping("/value")
    public String value() {
        return "user";
    }


}
