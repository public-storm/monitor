package com.zwy.monitor.web.request.file;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 检查文件上传请求参数体
 * @author zwy
 * @date 2022年04月25日 15:54
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class CheckUploadRequest {

    /**
     * 文件名
     */
    @NotBlank
    private String filename;

    /**
     * 上级文件id
     */
    @NotBlank
    private String superId;

    /**
     * 文件大小
     */
    @NotNull
    private Long fileSize;

    private String userId;
}
