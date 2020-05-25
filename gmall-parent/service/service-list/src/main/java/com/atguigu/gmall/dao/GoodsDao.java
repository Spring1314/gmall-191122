package com.atguigu.gmall.dao;

import com.atguigu.gmall.model.list.Goods;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author Administrator
 * @create 2020-05-20 11:43
 * 添加、修改、删除
 */
public interface
GoodsDao extends ElasticsearchRepository<Goods,Long> {
}
