package com.atguigu.gmall.user.Controler;

import com.atguigu.gmall.model.user.UserAddress;
import com.atguigu.gmall.user.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author Administrator
 * @create 2020-05-27 11:44
 * 用户管理
 */
@RestController
@RequestMapping("/api/user")
public class UserApiController {

    @Autowired
    private UserService userService;

    //获取用户地址
    @GetMapping("/inner/findUserAddressListByUserId/{userId}")
    public List<UserAddress> findUserAddressListByUserId(@PathVariable("userId") Long userId){
        return userService.findUserAddressListByUserId(userId);
    }
}
