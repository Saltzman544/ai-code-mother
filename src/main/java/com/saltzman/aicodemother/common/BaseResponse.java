package com.saltzman.aicodemother.common;

import com.saltzman.aicodemother.exception.ErrorCode;
import lombok.Data;

import java.io.Serializable;

/**
 * @Author: Saltzman
 * @Date: 2025/09/16/12:22
 * @Description:
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    public BaseResponse(int code, T data, String message) {
        this.code = code;
        this.data = data;
        this.message = message;
    }

    public BaseResponse(int code, T data) {
        this(code, data, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage());
    }
}

