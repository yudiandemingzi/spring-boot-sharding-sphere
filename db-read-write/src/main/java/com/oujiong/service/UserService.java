package com.oujiong.service;

import com.oujiong.entity.User;

import java.util.List;

/**
 * @author xub
 * @Description: 用户相关接口
 * @date 2019/8/24 下午6:32
 */
public interface UserService {

    /**
     * 获取所有用户信息
     */
    List<User> list();

    /**
     * 单个 保存用户信息
     *
     * @param user
     */
    String saveOne(User user);

}