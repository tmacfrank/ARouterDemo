package com.demo.arouter.api.core;

public interface ILoadParameter {

    /**
     * 通过以下方式完成赋值：
     * 目标对象.属性名 = getIntent().属性类型("注解值or属性名");
     *
     * @param target 目标对象，如：MainActivity
     */
    void loadParameter(Object target);
}
