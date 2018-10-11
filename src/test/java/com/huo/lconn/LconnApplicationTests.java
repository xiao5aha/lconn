package com.huo.lconn;

import org.apache.curator.framework.CuratorFramework;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LconnApplicationTests {
    @Autowired
    private CuratorFramework curatorFramework;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate1;

    @Autowired
    private RedisTemplate<String, String> redisTemplate2;

    @Autowired
    private RedisTemplate<String, byte[]> redisTemplate3;

    @Test
    public void test() throws Exception {
        System.out.println(curatorFramework.getChildren().forPath("/"));
    }

    @Test
    public void testRedis() {
        redisTemplate1.opsForValue().set("huohuo", "hahahha");
        redisTemplate1.opsForValue().get("huohuo");
        redisTemplate2.opsForValue().get("huohuo");
        System.out.println(redisTemplate1.opsForValue().get("huohuo") + "===" + redisTemplate2.opsForValue().get("huohuo") + "====" + redisTemplate3.opsForValue().get("huohuo"));
    }
}
