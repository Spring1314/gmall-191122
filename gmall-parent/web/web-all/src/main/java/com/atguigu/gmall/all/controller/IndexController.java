package com.atguigu.gmall.all.controller;

import com.atguigu.gmall.common.result.Result;
import com.atguigu.gmall.product.client.ProductFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ClassUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.io.*;
import java.util.List;
import java.util.Map;

/**
 * @author Administrator
 * @create 2020-05-19 17:02
 * 商品详情页面之商品分类渲染
 */
@Controller
public class IndexController {

    @Autowired
    private ProductFeignClient productFeignClient;
    @Autowired
    private SpringTemplateEngine templateEngine;

    /*//获得所有的分类属性
    @GetMapping("/")
    public String index(Model model){
        List<Map> baseCategoryList = productFeignClient.getBaseCategoryList();
        model.addAttribute("list",baseCategoryList);
        return "index/index";
    }*/

    //生成静态页面到指定位置
    @GetMapping("/createHtml")
    @ResponseBody
    public Result createHtml (){
        Context context = new Context();
        List<Map> baseCategoryList = productFeignClient.getBaseCategoryList();
        //数据
        context.setVariable("list",baseCategoryList);
        Writer out = null;
        try {
            String templates = ClassUtils.getDefaultClassLoader().getResource("templates").getPath();
            //设置字符编码，防止出现乱码
            //输出到指定位置
            out = new PrintWriter(templates + "/index.html","utf-8");
            //第一个参数是模板
            templateEngine.process("index/index",context,out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null){
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return Result.ok();
    }

    //访问静态页面
    @GetMapping("/")
    public String index(){
        return "index";
    }
}
