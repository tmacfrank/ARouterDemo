package com.demo.order;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.arouter.annotation.Autowired;
import com.demo.arouter.annotation.Route;
import com.demo.arouter.api.manager.ARouter;


@Route(path = "/order/Order_MainActivity")
public class Order_MainActivity extends AppCompatActivity {

    @Autowired
    String name;

    @Autowired(name = "agex")
    int age = 6;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_main);

        ARouter.getInstance().inject(this);
        Log.d("Test", "name: " + name + ",age: " + age);
    }

    public void jumpToPersonal(View view) {
        ARouter.getInstance().build("/personal/Personal_MainActivity")
                .withString("key1", "TestString")
                .withInt("key2", 66)
                .navigation(this);
    }
}
