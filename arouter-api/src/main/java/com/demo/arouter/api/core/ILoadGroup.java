package com.demo.arouter.api.core;

import java.util.Map;

/**
 * 路由组Group加载数据接口
 */
public interface ILoadGroup {

    /**
     * 加载路由组Group数据
     * 比如："app", ARouter$$Path$$app.class（实现了ILoadPath接口）
     *
     * @return key:"app", value:"app"分组对应的路由详细对象类
     */
    Map<String, Class<? extends ILoadPath>> loadGroup();
}
