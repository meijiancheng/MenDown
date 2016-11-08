package com.mjc.mendown.view;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Created by mjc on 2016/2/29.
 * 辅助绘制障碍物的类
 */
public class Barrier {
    //绘制的位置纵坐标
    public int mPositionY;
    public int mPositionX;
    //障碍物的宽度
    private int mWidth;
    //障碍物的高度
    private int mHeight;
    //屏幕的宽度
    private int mScreenWidth;

    private Paint mPaint;
    //当前的障碍物类型


    public Barrier(int screenWidth, Paint paint) {
        this.mScreenWidth = screenWidth;
        this.mPaint = paint;
        this.mWidth = mScreenWidth / 4;
    }

    /**
     * 绘制一个黑色矩形
     * @param canvas
     */
    public void drawBarrier(Canvas canvas) {
        canvas.save();
        RectF rectF = new RectF(mPositionX, mPositionY, mWidth + mPositionX, mPositionY + mHeight);
        canvas.drawRect(rectF, mPaint);
        canvas.restore();
    }

    public void setHeight(int mHeight) {
        this.mHeight = mHeight;
    }

    public int getWidth(){
        return this.mWidth;
    }


}
