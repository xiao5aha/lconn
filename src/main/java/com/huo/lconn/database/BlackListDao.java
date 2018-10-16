package com.huo.lconn.database;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Author: 小混蛋
 * @CreateDate: 2018/10/12 10:25
 */
@Mapper
public interface BlackListDao {
    String TABLE = "blacklist";
    String ALL_COL = "ip";

    @Select({"SELECT", ALL_COL, "FROM", TABLE})
    List<String> selectBlackList();
}
