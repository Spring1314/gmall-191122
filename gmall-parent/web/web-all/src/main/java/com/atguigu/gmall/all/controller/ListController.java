package com.atguigu.gmall.all.controller;

import com.alibaba.nacos.client.naming.utils.StringUtils;
import com.atguigu.gmall.client.ListFeignClient;
import com.atguigu.gmall.model.list.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * @author Administrator
 * @create 2020-05-22 21:27
 * 搜索管理
 */
@Controller
public class ListController {

    @Autowired
    private ListFeignClient listFeignClient;

    @GetMapping("/list.html")
    public String list(SearchParam searchParam, Model model){
        //1.入参对象回显
        model.addAttribute("searchParam",searchParam);
        SearchResponseVo responseVo = listFeignClient.list(searchParam);
        //平台属性集合
        List<SearchResponseAttrVo> attrsList = responseVo.getAttrsList();
        model.addAttribute("attrsList",attrsList);
        //品牌集合
        List<SearchResponseTmVo> trademarkList = responseVo.getTrademarkList();
        model.addAttribute("trademarkList",trademarkList);
        //商品集合
        List<Goods> goodsList = responseVo.getGoodsList();
        model.addAttribute("goodsList",goodsList);
        //pageNo totalPages
        model.addAttribute("pageNo",responseVo.getPageNo());
        model.addAttribute("totalPages",responseVo.getTotalPages());
        //urlParam
        String urlParam = makeUrlParam(searchParam);
        model.addAttribute("urlParam",urlParam);
        return "list/index";
    }

    private String makeUrlParam(SearchParam searchParam) {
        StringBuilder sb = new StringBuilder();
        String keyword = searchParam.getKeyword();
        if(!StringUtils.isEmpty(keyword)){
            sb.append("keyword=").append(keyword);
        }
        String trademark = searchParam.getTrademark();
        if(!StringUtils.isEmpty(trademark)){
            if(sb.length() > 0){
                sb.append("&trademark=").append(trademark);
            }else{
                sb.append("trademark=").append(trademark);
            }
        }
        Long category1Id = searchParam.getCategory1Id();
        if(null != category1Id){
            if(sb.length() > 0){
                sb.append("&category1Id=").append(category1Id);
            }else{
                sb.append("category1Id=").append(category1Id);
            }
        }
        Long category2Id = searchParam.getCategory2Id();
        if(null != category2Id){
            if(sb.length() > 0){
                sb.append("&category2Id=").append(category2Id);
            }else{
                sb.append("category2Id=").append(category2Id);
            }
        }
        Long category3Id = searchParam.getCategory3Id();
        if(null != category3Id){
            if(sb.length() > 0){
                sb.append("&category3Id=").append(category3Id);
            }else{
                sb.append("category3Id=").append(category3Id);
            }
        }
        //平台属性
        String[] props = searchParam.getProps();
        if(null != props && props.length > 0){
            for (String prop : props) {
                if(sb.length() > 0){
                    sb.append("&props=").append(prop);
                }else{
                    sb.append("props=").append(prop);
                }
            }
        }
        return "/list.html?" + sb.toString();
    }
}
