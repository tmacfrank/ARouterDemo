package com.demo.common.service;


import com.demo.arouter.api.core.IProvider;

public interface IOrderService extends IProvider {

    /**
     * @return 订单数量
     */
    int getOrderCount();
}
