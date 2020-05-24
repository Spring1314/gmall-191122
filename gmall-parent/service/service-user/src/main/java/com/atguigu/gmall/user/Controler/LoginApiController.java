package com.atguigu.gmall.user.Controler;

import com.atguigu.gmall.common.constant.RedisConst;
import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.model.user.UserInfo;
import com.atguigu.gmall.user.Service.LoginService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author Administrator
 * @create 2020-05-24 12:29
 * 登录管理
 */
@RestController
@RequestMapping("/api/user/passport")
public class LoginApiController {
    @Autowired
    private LoginService loginService;
    @Autowired
    private RedisTemplate redisTemplate;

    //登录
    @PostMapping("/login")
    public Result login(@RequestBody UserInfo userInfo){
        UserInfo userInfo1 = loginService.login(userInfo);
        if (userInfo1 == null){
            return Result.fail().message("用户名或密码不正确");
        } else {
            //生成令牌
            String token = UUID.randomUUID().toString().replace("-", "");
            //将令牌保存到redis中
            redisTemplate.opsForValue().set(RedisConst.USER_LOGIN_KEY_PREFIX + token,userInfo1.getId(),
                                             RedisConst.USERKEY_TIMEOUT, TimeUnit.SECONDS);
            Map map = new HashMap<>();
            String nickName = userInfo1.getNickName();
            map.put("token",token);
            map.put("nickName",nickName);
            return Result.ok(map);
        }
    }

    //退出登录
    @GetMapping("/logout")
    public Result logout(HttpServletRequest request){
        String token = request.getHeader("token");
        redisTemplate.delete(token);
        return Result.ok();
    }
}
