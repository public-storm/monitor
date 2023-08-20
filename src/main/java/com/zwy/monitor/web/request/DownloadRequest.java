package com.zwy.monitor.web.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2022年06月28日 14:12
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class DownloadRequest {
    @NotBlank
    private String id;
}
