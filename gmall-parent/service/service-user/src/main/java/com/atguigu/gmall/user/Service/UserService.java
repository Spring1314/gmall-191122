package com.atguigu.gmall.user.Service;

import com.atguigu.gmall.model.user.UserAddress;

import java.util.List;

/**
 * @author Administrator
 * @create 2020-05-27 11:48
 */

public interface UserService {
    List<UserAddress> findUserAddressListByUserId(Long userId);
}
