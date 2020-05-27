package com.atguigu.gmall.user.Service.impl;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.Service.UserService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author Administrator
 * @create 2020-05-27 11:48
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserAddressMapper userAddressMapper;

    //获取用户地址
    @Override
    public List<UserAddress> findUserAddressListByUserId(Long userId) {
        QueryWrapper<UserAddress> wrapper = new QueryWrapper<>();
        wrapper.eq("user_id",userId);
        return userAddressMapper.selectList(wrapper);
    }
}
