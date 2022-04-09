package com.demo.personal;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.arouter.annotation.Autowired;
import com.demo.arouter.annotation.Route;
import com.demo.arouter.api.manager.ARouter;
import com.demo.common.service.IOrderService;

@Route(path = "/personal/Personal_MainActivity")
public class Personal_MainActivity extends AppCompatActivity {

    @Autowired(name = "/order/OrderServiceImpl")
    IOrderService orderService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_main);

        ARouter.getInstance().inject(this);
        Toast.makeText(this, "订单数量：" + orderService.getOrderCount(), Toast.LENGTH_LONG).show();
    }
}
