package com.demo.order;

import com.demo.arouter.annotation.Route;
import com.demo.common.service.IOrderService;

@Route(path = "/order/OrderServiceImpl")
public class OrderServiceImpl implements IOrderService {

    @Override
    public int getOrderCount() {
        return 666;
    }
}
