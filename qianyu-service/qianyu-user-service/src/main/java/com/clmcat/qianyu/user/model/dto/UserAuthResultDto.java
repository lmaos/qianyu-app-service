package com.clmcat.qianyu.user.model.dto;

import com.clmcat.qianyu.user.model.entity.UserAuth;
import com.clmcat.qianyu.user.model.entity.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAuthResultDto {

    private UserAuth userAuth;
    private UserInfo userInfo;

}
