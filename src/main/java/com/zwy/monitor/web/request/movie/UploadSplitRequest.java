package com.zwy.monitor.web.request.movie;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * @author zwy
 * @date 2022年04月21日 9:17
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
@Slf4j
public class UploadSplitRequest {

    /**
     * 当前为第几分片 (必传）
     */
    @NotNull
    private Integer chunkNumber;
    /**
     * 文件总大小 (必传)
     */
    @NotNull
    private Long totalSize;

    /**
     * 分片总数
     */
    @NotNull
    private Integer totalChunks;

    /**
     * 分块文件传输对象
     */
    @NotNull
    private MultipartFile file;
    /**
     * 文件名
     */
    @NotBlank
    private String filename;
    /**
     * 合并成功发送id
     */
    @NotNull
    private Long webId;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 文件id（内部使用）
     */
    private String id;
}
