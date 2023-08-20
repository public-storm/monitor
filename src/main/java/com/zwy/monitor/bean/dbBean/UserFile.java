package com.zwy.monitor.bean.dbBean;

import lombok.*;

/**
 * @author zwy
 * @date 2022年05月17日 13:06
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class UserFile {
    private String id;
    private String name;
    private Integer file;
    private String superId;
    private String createTime;
    private String updateTime;
    private String userId;
    private Long size;
    private String suffix;
    private Integer status;
}
