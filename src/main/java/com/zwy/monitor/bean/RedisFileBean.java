package com.zwy.monitor.bean;

import lombok.*;

import java.io.Serializable;
import java.util.Set;

/**
 * @author zwy
 * @date 2022年05月19日 13:55
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString
@Builder
public class RedisFileBean implements Serializable {
    private static final long serialVersionUID = 5932737434724962764L;
    /**
     * 分片数
     */
    private Set<Integer> chunks;
    /**
     * 0：分片缺少  1：分片齐全
     */
    private int chunkStatus;
    /**
     * 0：待合并 1：合并成功
     */
    private int status;
}
