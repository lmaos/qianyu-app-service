package com.clmcat.qianyu.core.api;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class Rpc<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
    private int httpStatus = 200; // 允许透传到HTTP的参数
    private int code;
    private String state;
	private String msg;
	private T data;


    public boolean success() {
        return code == 0;
    }

}
