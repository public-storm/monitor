package com.zwy.monitor.web.request;

import lombok.*;

import javax.validation.constraints.NotBlank;

/**
 * @author zwy
 * @date 2022年07月08日 13:07
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class FindFavoriteRequest {
    @NotBlank
    private String id;
}
