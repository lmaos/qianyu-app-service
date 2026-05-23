package com.clmcat.qianyu.core.api;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class RpcResponse <T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private int code;
    private String state;
	private String msg;
	private T data;

}
