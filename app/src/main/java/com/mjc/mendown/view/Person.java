package com.mjc.mendown.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;

/**
 * Created by mjc on 2016/2/29.
 * 小人
 */
public class Person {
    public static final int SPEED = 10;
    private int mHeaderRadius;
    private Paint mPaint;
    //下边缘坐标
    public int mPersonY;
    public int mPersonX;
    private Bitmap bitmap;


    public Person(Paint paint, int radius, Bitmap bitmap) {
        this.mPaint = paint;
        this.mHeaderRadius = radius;
        this.bitmap = bitmap;
    }

    /**
     * 绘制一个圆形图片，带小人,贴吧滑稽表情,嘿嘿嘿
     * @param canvas
     */
    public void draw(Canvas canvas) {

        canvas.save();
        Path path = new Path();
        path.addCircle(mPersonX + mHeaderRadius, mPersonY + mHeaderRadius, mHeaderRadius, Path.Direction.CCW);

        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, null, new RectF(mPersonX, mPersonY, mPersonX + 2 * mHeaderRadius, mPersonY + mHeaderRadius * 2), mPaint);

        canvas.restore();
    }




}
