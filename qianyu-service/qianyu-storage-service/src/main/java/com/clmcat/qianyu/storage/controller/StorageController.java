package com.clmcat.qianyu.storage.controller;

import com.clmcat.framework.webmvc.anns.ApiController;
import com.clmcat.framework.webmvc.anns.LoginVerify;
import com.clmcat.framework.webmvc.anns.Params;
import com.clmcat.framework.webmvc.anns.Token;
import com.clmcat.qianyu.storage.config.StorageConfig;
import com.clmcat.qianyu.storage.model.UploadResult;
import com.clmcat.qianyu.storage.model.dto.StorageDeleteDto;
import com.clmcat.qianyu.storage.model.dto.StoragePresignDto;
import com.clmcat.qianyu.storage.model.status.Status;
import com.clmcat.qianyu.storage.model.vo.PresignResultVo;
import com.clmcat.qianyu.storage.service.StorageServiceBiz;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

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
    private StorageServiceBiz storageServiceBiz;

    @Resource
    private StorageConfig storageConfig;

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
    public UploadResult upload(
            @Parameter(hidden = true) @Token long userId,
            @Parameter(description = "上传的文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "存储平台（可选）") @RequestParam(value = "platform", required = false) String platform,
            @Parameter(description = "存储路径前缀") @RequestParam(value = "path", defaultValue = "upload") String path) {

        // 1. 空文件校验
        Status.FILE_EMPTY.assertThrowResEx(file.isEmpty());

        // 2. 文件大小校验
        Status.FILE_TOO_LARGE.assertThrowResEx(file.getSize() > storageConfig.getMaxFileSize());

        // 3. 文件扩展名校验
        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        List<String> allowed = storageConfig.getAllowedExtensions();
        Status.FILE_TYPE_NOT_ALLOWED.assertThrowResEx(
                allowed != null && !allowed.isEmpty() && !allowed.contains(extension.toLowerCase()));

        // 4. 生成对象 key: {path}/{年/月/日}/{uuid}.{ext}
        String key = buildObjectKey(path, extension);
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        // 5. 执行上传
        try {
            UploadResult result;
            if (platform != null && !platform.isBlank()) {
                result = storageServiceBiz.upload(platform, key, file.getInputStream(), contentType, file.getSize());
            } else {
                result = storageServiceBiz.upload(key, file.getInputStream(), contentType, file.getSize());
            }
            log.info("文件上传成功: userId={}, key={}, platform={}", userId, key, result.getPlatform());
            return result;
        } catch (Exception e) {
            log.error("文件上传失败: userId={}, key={}", userId, key, e);
            throw Status.STORAGE_UPLOAD_FAIL.apiEx();
        }
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
            @RequestBody(description = "文件删除参数") @Params StorageDeleteDto dto) {

        Status.STORAGE_KEY_REQUIRED.assertThrowResEx(dto.getKey() == null || dto.getKey().isBlank());

        try {
            if (dto.getPlatform() != null && !dto.getPlatform().isBlank()) {
                storageServiceBiz.delete(dto.getPlatform(), dto.getKey());
            } else {
                storageServiceBiz.delete(dto.getKey());
            }
            log.info("文件删除成功: userId={}, key={}", userId, dto.getKey());
            return true;
        } catch (Exception e) {
            log.error("文件删除失败: userId={}, key={}", userId, dto.getKey(), e);
            throw Status.STORAGE_DELETE_FAIL.apiEx();
        }
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

        Status.STORAGE_KEY_REQUIRED.assertThrowResEx(dto.getKey() == null || dto.getKey().isBlank());

        long expireSeconds = dto.getExpireSeconds() != null ? dto.getExpireSeconds() : 3600L;

        try {
            String url;
            if (dto.getPlatform() != null && !dto.getPlatform().isBlank()) {
                url = storageServiceBiz.generatePresignedUrl(dto.getPlatform(), dto.getKey(), expireSeconds);
            } else {
                url = storageServiceBiz.generatePresignedUrl(dto.getKey(), expireSeconds);
            }
            return PresignResultVo.builder()
                    .url(url)
                    .expireSeconds(expireSeconds)
                    .build();
        } catch (Exception e) {
            log.error("预签名链接生成失败: userId={}, key={}", userId, dto.getKey(), e);
            throw Status.STORAGE_PRESIGN_FAIL.apiEx();
        }
    }

    // ===== 私有方法 =====

    /**
     * 生成存储对象 key
     * 格式: {path}/{年/月/日}/{uuid}.{ext}
     */
    private String buildObjectKey(String path, String extension) {
        String datePath = String.format("%tY/%<tm/%<td", System.currentTimeMillis());
        String fileName = UUID.randomUUID().toString().replace("-", "") + "." + extension;
        return path + "/" + datePath + "/" + fileName;
    }

    /**
     * 提取文件扩展名
     */
    private String extractExtension(String filename) {
        if (filename == null || filename.isBlank()) {
            return "bin";
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return "bin";
        }
        return filename.substring(dotIndex + 1);
    }
}
