package com.example.zhanghq.swipetest;

import com.example.zhanghq.swipetest.R;


import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Administrator on 2015/6/26 0026.
 */
public class MainActivity extends Activity {
    private SwipeRelativeLayout mSwipViewRl;
    private TextView mSwipTv;
    private ImageView mDeleteIv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.e("swiptest", "onCreate");
        mSwipViewRl = (SwipeRelativeLayout)findViewById(R.id.rl_swip);
        mSwipTv = (TextView)findViewById(R.id.tv_swip);
        mSwipTv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e("mSwipTv", "onClick");
                Toast.makeText(MainActivity.this, "TextView Click!", Toast.LENGTH_SHORT).show();
            }
        });
        mDeleteIv = (ImageView)findViewById(R.id.iv_delete);
        mDeleteIv.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.e("mSwipTv", "onClick");
                Toast.makeText(MainActivity.this, "MenuView Click!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mSwipViewRl.isShowingMenu()){
            mSwipViewRl.HideMenu();
        }
        return super.onTouchEvent(event);
    }
}
