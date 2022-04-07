package com.demo.personal;

import androidx.appcompat.app.AppCompatActivity;

import com.demo.arouter.annotation.Route;

@Route(path = "/personal/Personal_MainActivity")
public class Personal_MainActivity extends AppCompatActivity {

    /*@Autowired
    IOrderService orderService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_personal_main);

        ARouter.getInstance().inject(this);

        int backlogCount = orderService.getBacklogCount();
        int sendingCount = orderService.getSendingCount();
        int evaluatingCount = orderService.getEvaluatingCount();
        Toast.makeText(this, "待发货：" + backlogCount + "，待收货：" + sendingCount +
                "，待评价：" + evaluatingCount, Toast.LENGTH_LONG).show();
    }*/
}
