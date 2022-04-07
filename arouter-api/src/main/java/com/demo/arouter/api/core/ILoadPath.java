package com.demo.arouter.api.core;

import com.demo.arouter.model.RouteMeta;

import java.util.Map;

/**
 * 路由组Group对应的详细Path加载数据接口
 * 比如：app分组对应有哪些类需要加载
 */
public interface ILoadPath {

    /**
     * 加载路由组Group中的Path详细数据
     * 比如："app"分组下有这些信息：
     *
     * @return key:"/app/MainActivity", value:封装了MainActivity信息的RouteMeta
     */
    Map<String, RouteMeta> loadPath();
}
