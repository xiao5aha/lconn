package com.huo.lconn.cache.rediscache.blacklist;

import com.huo.lconn.constant.Constant;
import com.huo.lconn.database.BlackListDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/10/15 14:35
 */
@Service
public class BlackListService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private BlackListDao blackListDao;

    public void initBlackList() {
        List<String> blackList = blackListDao.selectBlackList();
        blackList.parallelStream().forEach(ip -> redisTemplate.opsForSet().add(Constant.BLACK_LIST, ip));
    }

    public boolean isInBlackList(String ip) {
        return redisTemplate.opsForSet().isMember(Constant.BLACK_LIST, ip);
    }

}
