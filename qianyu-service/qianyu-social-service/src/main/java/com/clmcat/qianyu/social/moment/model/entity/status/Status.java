package com.clmcat.qianyu.social.moment.model.entity.status;

import com.clmcat.framework.webmvc.ResponseErrorStatus;
import com.clmcat.framework.webmvc.ResponseStatus;


import java.io.Serializable;



/**
 * loChat 状态
 */
public enum Status implements ResponseErrorStatus, Serializable {


    OK(ResponseStatus.OK.getStatus(), "OK", "一个成功的请求"),
    /** 作者ID必填 */
    AUTHOR_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "作者ID必填", "作者ID必填"),
    /** 作品ID必填 */
    MOMENT_ID_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "作品ID必填", "作品ID必填"),
    /** 作品内容必填 */
    MOMENT_CONTENT_REQUIRED(ResponseStatus.P_VALUE_ERROR.getStatus(), "作品 content 必填", "作品 content 必填"),
    /** 作品不存在 */
    MOMENT_NOT_FOUND(ResponseStatus.R_NOEXIST_DATA.getStatus(), "作品不存在", "作品不存在"),
    /** 作品发布失败 */
    MOMENT_SAVE_FAIL(ResponseStatus.R_OPERATION_FAIL.getStatus(), "作品发布失败", "作品发布失败"),
    /** 作品删除失败 */
    MOMENT_DELETE_FAIL(ResponseStatus.R_OPERATION_FAIL.getStatus(), "作品删除失败", "作品删除失败"),
    /** 无权删除作品 */
    MOMENT_DELETE_DENIED(ResponseStatus.A_ACCESS_DENIED.getStatus(), "无权删除该作品", "无权删除该作品"),
    /** 作品类型错误 */
    MOMENT_TYPE_ERROR(ResponseStatus.P_VALUE_ERROR.getStatus(), "作品类型错误", "作品类型错误"),
    /** 作品图片数据必须填, imageUrl, width, height */
    MOMENT_IMAGE_URL_REQUIRED(Status.MOMENT_ID_REQUIRED.getStatus(), "图片URL必填"),
    /** 图片 宽度 必填 */
    MOMENT_IMAGE_WIDTH_REQUIRED(Status.MOMENT_ID_REQUIRED.getStatus(), "图片width必填"),
    /** 图片高度必填 */
    MOMENT_IMAGE_HEIGHT_REQUIRED(Status.MOMENT_ID_REQUIRED.getStatus(), "图片height必填"),

    /** 作品视频数据必须填, videoUrl, coverUrl, width, height */
    MOMENT_VIDEO_URL_REQUIRED(Status.MOMENT_ID_REQUIRED.getStatus(), "视频URL必填"),
    /** 视频封面URL必填 */
    MOMENT_VIDEO_COVER_URL_REQUIRED(Status.MOMENT_ID_REQUIRED.getStatus(), "视频封面URL必填"),
    /** 视频宽度必填 */
    MOMENT_VIDEO_WIDTH_REQUIRED(Status.MOMENT_ID_REQUIRED.getStatus(), "视频width必填"),
    /** 视频高度必填 */
    MOMENT_VIDEO_HEIGHT_REQUIRED(Status.MOMENT_ID_REQUIRED.getStatus(), "视频height必填"),
    /** 文本必填 */
    MOMENT_CONTENT_TEXT_REQUIRED(Status.MOMENT_ID_REQUIRED.getStatus(), "文本必须填写"),





    ;
    Status(Integer status, String message) {
        this.status = status;
        this.message = message;
    }

    Status(Integer status, String message, String describe) {
        this.status = status;
        this.message = message;
        this.describe = describe;
    }

    private int httpStatus = 200; // HTTP 状态码
    private Integer status; // HTTP 状态码
    private String message; // HTTP 错误内容
    private String describe;


    @Override
    public int getHttpStatus() {
        return httpStatus;
    }

    @Override
    public String getState() {
        return name();
    }

    @Override
    public Integer getStatus() {
        return status;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
