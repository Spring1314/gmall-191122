package com.atguigu.gmall.user.Service.impl;

import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.Service.LoginService;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author Administrator
 * @create 2020-05-24 12:32
 */
@Service
public class LoginServiceImpl implements LoginService {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public UserInfo login(UserInfo userInfo) {
        //认证
        QueryWrapper<UserInfo> wrapper = new QueryWrapper<>();
        String digest = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        wrapper.eq("login_name",userInfo.getLoginName()).eq("passwd",digest);
        return userInfoMapper.selectOne(wrapper);
    }
}
