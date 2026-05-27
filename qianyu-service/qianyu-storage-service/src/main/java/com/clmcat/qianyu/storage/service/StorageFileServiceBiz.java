package com.clmcat.qianyu.storage.service;

import com.clmcat.qianyu.storage.config.StorageConfig;
import com.clmcat.qianyu.storage.mapper.StorageFileMapper;
import com.clmcat.qianyu.storage.model.UploadResult;
import com.clmcat.qianyu.storage.model.dto.StorageDeleteDto;
import com.clmcat.qianyu.storage.model.dto.StoragePresignDto;
import com.clmcat.qianyu.storage.model.entity.StorageFile;
import com.clmcat.qianyu.storage.model.status.Status;
import com.clmcat.qianyu.storage.model.vo.PresignResultVo;
import com.clmcat.qianyu.storage.model.vo.StorageUploadVo;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 文件存储业务服务
 * <p>
 * 负责文件校验、上传协调、记录持久化、删除和预签名链接生成。
 * Controller 仅做参数绑定，所有业务逻辑在此层完成。
 *
 * @author author
 * @date 2025-01-01
 */
@Slf4j
@Service
public class StorageFileServiceBiz {

    @Resource
    private StorageFileMapper storageFileMapper;

    @Resource
    private StorageServiceBiz storageServiceBiz;

    @Resource
    private StorageConfig storageConfig;

    // ===== 上传 =====

    /**
     * 上传文件并持久化记录
     *
     * @param userId   上传用户ID
     * @param file     上传的文件
     * @param platform 存储平台（可选）
     * @param path     存储路径前缀（可选，默认 upload）
     * @return 上传结果 VO
     */
    public StorageUploadVo upload(long userId, MultipartFile file, String platform, String path) {
        // 1. 校验
        validateFile(file);
        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        validateExtension(extension);

        // 2. 生成存储 key
        String resolvedPath = (path != null && !path.isBlank()) ? path : "upload";
        String key = buildObjectKey(resolvedPath, extension);
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        // 3. 执行上传
        UploadResult result;
        try {
            if (platform != null && !platform.isBlank()) {
                result = storageServiceBiz.upload(platform, key, file.getInputStream(), contentType, file.getSize());
            } else {
                result = storageServiceBiz.upload(key, file.getInputStream(), contentType, file.getSize());
            }
        } catch (Exception e) {
            log.error("文件上传失败: userId={}, key={}", userId, key, e);
            throw Status.STORAGE_UPLOAD_FAIL.apiEx();
        }

        // 4. 持久化记录
        StorageFile record = StorageFile.builder()
                .userId(userId)
                .originalName(originalFilename != null ? originalFilename : "")
                .fileKey(result.getKey())
                .fileUrl(result.getUrl())
                .fileType(extension.toLowerCase())
                .contentType(contentType)
                .fileSize(file.getSize())
                .platform(result.getPlatform())
                .createTime(System.currentTimeMillis())
                .createTimeServer(LocalDateTime.now())
                .build();

        storageFileMapper.insertSelective(record);
        log.info("文件上传成功: userId={}, key={}, id={}", userId, key, record.getId());

        // 5. 构建返回
        return StorageUploadVo.builder()
                .id(record.getId())
                .url(result.getUrl())
                .key(result.getKey())
                .platform(result.getPlatform())
                .fileType(extension.toLowerCase())
                .build();
    }

    // ===== 删除 =====

    /**
     * 删除文件
     *
     * @param userId 当前用户ID
     * @param dto    删除参数
     * @return 是否成功
     */
    public boolean delete(long userId, StorageDeleteDto dto) {
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

    // ===== 预签名 =====

    /**
     * 生成预签名访问 URL
     *
     * @param userId 当前用户ID
     * @param dto    预签名参数
     * @return 预签名结果
     */
    public PresignResultVo presign(long userId, StoragePresignDto dto) {
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

    // ===== 内部方法 =====

    /**
     * 校验文件：空文件 + 大小限制
     */
    private void validateFile(MultipartFile file) {
        Status.FILE_EMPTY.assertThrowResEx(file.isEmpty());
        Status.FILE_TOO_LARGE.assertThrowResEx(file.getSize() > storageConfig.getMaxFileSize());
    }

    /**
     * 校验文件扩展名是否在白名单内
     */
    private void validateExtension(String extension) {
        List<String> allowed = storageConfig.getAllowedExtensions();
        Status.FILE_TYPE_NOT_ALLOWED.assertThrowResEx(
                allowed != null && !allowed.isEmpty() && !allowed.contains(extension.toLowerCase()));
    }

    /**
     * 生成存储对象 key
     * <p>
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
