package com.demo.order;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.arouter.annotation.Route;
import com.demo.arouter.api.manager.ARouter;


@Route(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_main);
    }

    public void jumpToPersonal(View view) {
        ARouter.getInstance().build("/personal/Personal_MainActivity")
                .withString("key1", "TestString")
                .withInt("key2", 66)
                .navigation(this);
    }
}
