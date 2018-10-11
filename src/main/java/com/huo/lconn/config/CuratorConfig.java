package com.huo.lconn.config;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/9/26 11:18
 */
@Configuration
public class CuratorConfig {

    @Value("${spring.zookeeper.server}")
    public String server;

    @Bean
    public CuratorFramework getClient() {
        CuratorFramework instance = CuratorFrameworkFactory.newClient(server, new ExponentialBackoffRetry(1000, 3));
        instance.start();
        return instance ;
    }
}
