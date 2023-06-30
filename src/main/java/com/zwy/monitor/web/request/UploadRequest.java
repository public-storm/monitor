package com.zwy.monitor.web.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;
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
public class UploadRequest {

    @ApiModelProperty(value = "当前为第几分片", required = true)
    @NotNull
    private Integer chunkNumber;

    @ApiModelProperty(value = "每个分块的大小", required = true)
    @NotNull
    private Long chunkSize;

    @ApiModelProperty(value = "当前分块大小", required = true)
    @NotNull
    private Integer currentChunkSize;

    @ApiModelProperty(value = "文件总大小", required = true)
    @NotNull
    private Long totalSize;

    @ApiModelProperty(value = "文件名 带后缀", required = true)
    @NotBlank
    private String filename;

    @ApiModelProperty(value = "上级文件id", required = true)
    @NotBlank
    private String superId;

    @ApiModelProperty(value = "分片总数", required = true)
    @NotNull
    private Integer totalChunks;

    @ApiModelProperty(value = "分块文件传输对象", required = true)
    @NotNull
    private MultipartFile file;

}
