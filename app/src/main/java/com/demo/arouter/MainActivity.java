package com.demo.arouter;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.arouter.annotation.Route;
import com.demo.arouter.api.manager.ARouter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void jumpToOrder(View view) {
        ARouter.getInstance().build("/order/Order_MainActivity")
                .withString("key","value")
                .navigation(this);
    }
}