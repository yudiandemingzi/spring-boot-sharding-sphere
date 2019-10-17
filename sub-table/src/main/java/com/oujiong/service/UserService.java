package com.oujiong.service;

import com.oujiong.entity.User;

import java.util.List;

/**
 * @Description: 用户相关接口
 *
 * @author xub
 * @date 2019/10/10 下午8:53
 */
public interface UserService {

    /**
     * 获取所有用户信息
     */
    List<User>  list();

    /**
     *   批量 保存用户信息
     * @param userVOList
     */
    String  insertForeach(List<User> userVOList);

}