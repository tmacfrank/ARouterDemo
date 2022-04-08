package com.demo.arouter;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.arouter.annotation.Route;
import com.demo.arouter.api.manager.ARouter;

@Route(path = "/app/MainActivity")
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jumpToOrder(View view) {
        ARouter.getInstance().build("/order/Order_MainActivity")
                .withInt("agex", 29)
                .withString("name", "Angela")
                .navigation(this);
    }
}