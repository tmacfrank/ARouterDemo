package com.demo.arouter.enums;

import com.demo.arouter.utils.Constants;

/**
 * 表示路由的类型，如 ACTIVITY 表示跳转，PROVIDER 表示跨模块调用服务
 * 也可以扩展 SERVICE、FRAGMENT 出等类型
 */
public enum RouteType {

    ACTIVITY(0, Constants.PACKAGE_NAME_ACTIVITY),
    PROVIDER(1,Constants.PACKAGE_NAME_IPROVIDER);

    int id;
    String className;

    RouteType(int id, String className) {
        this.id = id;
        this.className = className;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
}
