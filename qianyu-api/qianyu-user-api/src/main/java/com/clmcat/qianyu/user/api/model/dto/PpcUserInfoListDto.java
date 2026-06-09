package com.clmcat.qianyu.user.api.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class PpcUserInfoListDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public static final PpcUserInfoListDto EMPTY = PpcUserInfoListDto.builder().users(new ArrayList<>()).build();

    private List<RpcUserInfoDto> users;
}
