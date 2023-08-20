package com.zwy.monitor.web.request;

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
public class UploadRequest {
    @NotNull
    private MultipartFile file;
    @NotBlank
    private String filename;
    @NotBlank
    private String superId;
    @NotBlank
    private String id;
    private String userId;
}
