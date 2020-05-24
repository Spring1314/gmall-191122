package com.atguigu.gmall.all.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * @author Administrator
 * @create 2020-05-24 13:46
 */
@Controller
public class LoginController {

    //去登陆页面
    @GetMapping("/login.html")
    public String login(String originUrl, Model model) {
        model.addAttribute("originUrl",originUrl);
        return "login";
    }


}
