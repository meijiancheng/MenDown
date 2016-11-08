package com.mjc.mendown;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.mjc.mendown.util.OnContinueClickListener;
import com.mjc.mendown.view.GameLayout;

/**
 * 这个小游戏还是不完整的，还可以加入重力加速度
 * 但是，这里最重要的意义不是要做一个游戏，
 * 而是在于，演示如何去自定义一个View,游戏类，就是一个自定义的View,
 * 如果真要考虑游戏性能，可以考虑SurfaceView
 */
public class MainActivity extends AppCompatActivity {

    private GameLayout mGameLayout;
    private View left;
    private View right;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mGameLayout = (GameLayout) findViewById(R.id.game);
        left = findViewById(R.id.left);
        right = findViewById(R.id.right);

        left.setOnTouchListener(new OnContinueClickListener() {
            @Override
            public void handleClickEvent(View view) {
                mGameLayout.moveLeft();
            }
        });


        right.setOnTouchListener(new OnContinueClickListener() {
            @Override
            public void handleClickEvent(View view) {
                mGameLayout.moveRight();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mGameLayout.stop();
    }


}
