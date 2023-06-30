package com.zwy.monitor;

import com.zwy.monitor.util.TableInit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.annotation.Resource;
import java.io.File;
import java.util.List;

/**
 * @author zwy
 */
@Configuration
@Order(value = 1)
@Slf4j
public class AppStartup implements ApplicationRunner {


    @Value("${file.path}")
    private String path;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        //存储目录创建
        createLocalPath(path);
        //初始化表
        createTable();
    }

    /**
     * 存储目录创建
     *
     * @param path 存储目录
     */
    private void createLocalPath(String path) {
        File file = new File(path);
        if (!file.exists() && !file.isDirectory()) {
            boolean create = file.mkdirs();
            log.info("file path create {}", create);
        }
    }

    private void createTable() {
        List<String> sqlList = TableInit.findSqlList();
        if (!sqlList.isEmpty()) {
            sqlList.forEach(sql -> {
                if (!sql.isEmpty()) {
                    jdbcTemplate.execute(sql);
                }
            });
        }
    }
}
