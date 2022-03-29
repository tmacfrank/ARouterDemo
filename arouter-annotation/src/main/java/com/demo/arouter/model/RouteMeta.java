package com.demo.arouter.model;

import com.demo.arouter.enums.RouteType;

import javax.lang.model.element.Element;

public class RouteMeta {

    // 该 JavaBean 的事件类型，如 ACTIVITY 表示跳转，PROVIDER 表示跨模块调用服务
    private RouteType mRouteType;
    // 节点类型
    private Element mElement;
    // 路由组
    private String mGroup;
    // 路由地址
    private String mPath;
    // 被注解标记的类对象
    private Class<?> mTargetClass;

    private RouteMeta(RouteType routeType, String group, String path, Class<?> targetClass) {
        mRouteType = routeType;
        mGroup = group;
        mPath = path;
        mTargetClass = targetClass;
    }

    private RouteMeta(Builder builder) {
        mRouteType = builder.mRouteType;
        mElement = builder.mElement;
        mGroup = builder.mGroup;
        mPath = builder.mPath;
        mTargetClass = builder.mTargetClass;
    }

    // 对外提供简易版构造方法，主要是为了方便 APT 生成代码
    public static RouteMeta create(RouteType routeType, String group, String path, Class<?> targetClass) {
        return new RouteMeta(routeType, group, path, targetClass);
    }

    public RouteType getRouteType() {
        return mRouteType;
    }

    public void setRouteType(RouteType routeType) {
        mRouteType = routeType;
    }

    public Element getElement() {
        return mElement;
    }

    public void setElement(Element element) {
        mElement = element;
    }

    public String getGroup() {
        return mGroup;
    }

    public void setGroup(String group) {
        mGroup = group;
    }

    public String getPath() {
        return mPath;
    }

    public void setPath(String path) {
        mPath = path;
    }

    public Class<?> getTargetClass() {
        return mTargetClass;
    }

    public void setTargetClass(Class<?> targetClass) {
        mTargetClass = targetClass;
    }

    public static class Builder {

        private RouteType mRouteType;
        private Element mElement;
        private String mGroup;
        private String mPath;
        private Class<?> mTargetClass;

        public Builder setRouteType(RouteType routeType) {
            mRouteType = routeType;
            return this;
        }

        public Builder setElement(Element element) {
            mElement = element;
            return this;
        }

        public Builder setGroup(String group) {
            mGroup = group;
            return this;
        }

        public Builder setPath(String path) {
            mPath = path;
            return this;
        }

        public Builder setTargetClass(Class<?> targetClass) {
            mTargetClass = targetClass;
            return this;
        }

        public RouteMeta build() {
            if (mPath == null || mPath.length() == 0) {
                throw new IllegalArgumentException("Path 为必填项，不能为空！如：/app/MainActivity");
            }
            return new RouteMeta(this);
        }
    }
}
