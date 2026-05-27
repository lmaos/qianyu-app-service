package com.clmcat.qianyu.storage.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.storage.model.dto.StorageDeleteDto;
import com.clmcat.qianyu.storage.model.dto.StoragePresignDto;
import com.clmcat.qianyu.storage.model.vo.PresignResultVo;
import com.clmcat.qianyu.storage.model.vo.StorageUploadVo;
import com.clmcat.qianyu.storage.service.StorageFileServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储接口。
 * <p>
 * 提供文件上传、删除和预签名链接生成能力。
 * <p>
 * OpenAPI 页面地址（默认本地启动）：
 * http://localhost:8080/swagger-ui/index.html
 * <p>
 * OpenAPI JSON 地址（HTTP API 分组）：
 * http://localhost:8080/v3/api-docs/http-api
 */
@Tag(name = "文件存储接口", description = "提供文件上传、删除和预签名链接生成能力。")
@ApiController
@RequestMapping("/api/storage")
@LoginVerify
@Slf4j
public class StorageController {

    @Resource
    private StorageFileServiceBiz storageFileServiceBiz;

    /**
     * 上传文件。
     *
     * @param userId   当前登录用户ID，来自 Token
     * @param file     上传的文件
     * @param platform 存储平台（可选）
     * @param path     存储路径前缀（可选，默认 upload）
     * @return 上传结果
     */
    @Operation(summary = "上传文件", description = "参数说明：userId 为当前登录用户ID；file 为上传文件；platform 可选存储平台；path 可选路径前缀，默认 upload。")
    @PostMapping("/upload")
    public StorageUploadVo upload(
            @Parameter(hidden = true) @Token long userId,
            @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "存储平台（可选）") @RequestParam(value = "platform", required = false) String platform,
            @Parameter(description = "存储路径前缀") @RequestParam(value = "path", defaultValue = "upload") String path) {

        return storageFileServiceBiz.upload(userId, file, platform, path);
    }

    /**
     * 删除文件。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto    删除参数，包含 key 和可选的 platform
     * @return 删除是否成功
     */
    @Operation(summary = "删除文件", description = "参数说明：userId 为当前登录用户ID；dto.key 为文件唯一标识；dto.platform 为可选存储平台。")
    @PostMapping("/delete")
    public boolean delete(
            @Parameter(hidden = true) @Token long userId,
            @ParameterObject @Params StorageDeleteDto dto) {

        return storageFileServiceBiz.delete(userId, dto);
    }

    /**
     * 生成预签名访问 URL。
     *
     * @param userId 当前登录用户ID，来自 Token
     * @param dto    预签名参数，包含 key、expireSeconds 和可选的 platform
     * @return 预签名结果
     */
    @Operation(summary = "生成预签名链接", description = "参数说明：userId 为当前登录用户ID；dto.key 为文件唯一标识；dto.expireSeconds 为有效期（秒），默认 3600；dto.platform 为可选存储平台。")
    @GetMapping("/presign")
    public PresignResultVo presign(
            @Parameter(hidden = true) @Token long userId,
            @ParameterObject @Params StoragePresignDto dto) {

        return storageFileServiceBiz.presign(userId, dto);
    }
}
