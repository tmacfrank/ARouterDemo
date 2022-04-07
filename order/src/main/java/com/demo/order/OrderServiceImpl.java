package com.demo.order;

import com.demo.common.service.IOrderService;

/*@Route(path = "/order/OrderServiceImpl")*/
public class OrderServiceImpl implements IOrderService {

    @Override
    public int getBacklogCount() {
        return 4;
    }

    @Override
    public int getSendingCount() {
        return 3;
    }

    @Override
    public int getEvaluatingCount() {
        return 5;
    }

    /*@Override
    public void init(Context context) {

    }*/
}
