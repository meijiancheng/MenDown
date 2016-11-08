package com.mjc.mendown.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.mjc.mendown.R;
import com.mjc.mendown.util.PositionUtil;

import java.util.ArrayList;

/**
 * Created by mjc on 2016/3/3.
 */
public class GameLayout extends View {

    //当前视图(GameLayout)的长和宽
    private int mLayoutWidth;
    private int mLayoutHeight;
    //辅助绘制障碍物的对象
    private Barrier mBarrier;
    //辅助绘制人物的对象
    private Person mPerson;
    //面板绘制的对象
    private Score mScore;

    private Paint mPaint;
    //小人的圆形半径
    private int radius = 50;
    //不断绘制的线程
    private Thread mThread;

    private MyHandler myHandler;
    private int mBarrierMoveSpeed = 8;
    //人物是否自动下落状态
    private boolean isAutoFall;
    //游戏是否正在运行
    private boolean isRunning;
    //人物左右移动的速度
    private int mPersonMoveSpeed = 20;
    //需要绘制的小人
    private Bitmap bitmap;

    //画面中障碍物的位置信息
    private ArrayList<Integer> mBarrierXs;
    private ArrayList<Integer> mBarrierYs;
    //障碍物起始和产生障碍的间隔
    private int mBarrierStartY = 500;
    private int mBarrierInterval = 500;
    //障碍物的高度
    private int mBarrierHeight = 60;
    //人物所站立的障碍在画面中的index
    private int mTouchIndex = -1;

    //当小人自动下落瞬间，开始计时，单位毫秒
    private float mFallTime = 0;

    //重力加速度
    public static final float G = 9.8f;

    //总得分
    private int mTotalScore;
    //份数版块的文字大小
    private int mTextSize = 16;

    //失败后，弹出的菜单，按钮的位置
    private RectF mRestartRectf;
    private RectF mQuiteRectf;
    //按钮的宽度和高度，这里我省事没有转化为DP，都是直接用px，所以可能会
    //产生适配上的问题。
    private int mButtonWidth = 300;
    private int mButtonHeight = 120;
    private int Padding = 20;

    public GameLayout(Context context) {
        super(context);
        init();
    }

    public GameLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        //初始化画笔
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setColor(Color.GRAY);
        mPaint.setStrokeWidth(10);
        //读取本地的img图片
        bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.img);
        //默认开始自动下落
        isAutoFall = true;
        myHandler = new MyHandler();
        //用来记录画面中，每一个障碍物的x坐标
        mBarrierXs = new ArrayList<>();
        //和上面的x对应的每个障碍物的y坐标
        mBarrierYs = new ArrayList<>();
        //将文字大小转化成DP
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize, getResources().getDisplayMetrics());
        //启动游戏
        isRunning = true;
        startGame();
    }


    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        //当前方法，是在onMeasure调用之后，进行回调，所以直接getMeasureWidth等
        //获取当前视图的宽和高
        mLayoutWidth = getMeasuredWidth();
        mLayoutHeight = getMeasuredHeight();
        //根据视图宽高，初始化障碍物的信息
        mBarrier = new Barrier(mLayoutWidth, mPaint);
        mBarrier.setHeight(mBarrierHeight);
        //创建人物绘制类对象
        mPerson = new Person(mPaint, radius, bitmap);
        mPerson.mPersonY = 300;
        mPerson.mPersonX = mLayoutWidth / 2;
        //初始化分数绘制对象
        mScore = new Score(mPaint);
        mScore.x = mLayoutWidth / 2 - mScore.panelWidth / 2;

        //菜单上重启按钮的左边坐标,mRestartRectf是重启按钮绘制区域
        int rX = mLayoutWidth / 2 - 20 - mButtonWidth;
        int rY = mLayoutHeight * 3 / 5;
        mRestartRectf = new RectF(rX, rY, rX + mButtonWidth, rY + mButtonHeight);
        //下面是菜单上退出按钮的区域
        int qX = mLayoutWidth / 2 + 20;
        int qY = mLayoutHeight * 3 / 5;
        mQuiteRectf = new RectF(qX, qY, qX + mButtonWidth, qY + mButtonHeight);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //绘制分数面板
        generateScore(canvas);
        //绘制障碍物
        generateBarrier(canvas);
        //如果小人正在下落，才检测是否碰撞
        if (isAutoFall)
            checkTouch();
        //根据是否下落，绘制小人的位置
        generatePerson(canvas);
        //如果没有结束，说明就是在运行
        //检查小人是否超出边界，判断游戏是否结束
        isRunning = !checkIsGameOver();
        //如果游戏结束
        if (!isRunning) {
            //绘制面板
            drawPanel(canvas);
            //绘制游戏结束数字
            notifyGameOver(canvas);
            //绘制两个按钮
            drawButton(canvas, mRestartRectf, "重来", Color.parseColor("#ae999999"), Color.WHITE);
            drawButton(canvas, mQuiteRectf, "退出", Color.parseColor("#ae999999"), Color.WHITE);
        }
    }

    /**
     * 绘制结束弹出框的背景区域
     * @param canvas
     */
    private void drawPanel(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setColor(Color.parseColor("#8e333333"));
        canvas.drawRoundRect(new RectF(mRestartRectf.left - Padding * 2, mLayoutHeight * 2 / 5 - Padding, mQuiteRectf.right + Padding * 2, mQuiteRectf.bottom + Padding), Padding, Padding, mPaint);
    }

    /**
     * 绘制Game over文字
     * @param canvas
     */
    private void notifyGameOver(Canvas canvas) {
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(mTextSize * 1.5f);
        mPaint.setColor(Color.parseColor("#cc0000"));
        mPaint.setFakeBoldText(false);
        canvas.drawText("Game over", mLayoutWidth / 2, mLayoutHeight / 2, mPaint);
    }

    //绘制菜单按钮,下面的操作使得文字能够居中显示
    private void drawButton(Canvas canvas, RectF rectF, String text, int strokeColor, int textColor) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(strokeColor);
        canvas.drawRoundRect(rectF, 10, 10, mPaint);
        mPaint.setTextSize(mTextSize);
        mPaint.setColor(textColor);
        mPaint.setTextAlign(Paint.Align.CENTER);
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        float textHeight = fontMetrics.bottom - fontMetrics.top;
        int y = (int) (rectF.top + textHeight / 2 + (rectF.bottom - rectF.top) / 2 - fontMetrics.bottom);
        canvas.drawText(text, rectF.left + mButtonWidth / 2, y, mPaint);

    }

    /**
     * 绘制分数面板
     * @param canvas
     */
    private void generateScore(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.parseColor("#666666"));
        mScore.drawPanel(canvas);
        mPaint.setColor(Color.WHITE);
        mPaint.setFakeBoldText(true);
        mPaint.setTextSize(mTextSize);
        mScore.drawScore(canvas, mTotalScore + "");
    }

    /**据初始位置，生成障碍物,难点
     * 1.绘制时，每一个障碍物间的距离是一致的
     * 2.绘制时，都是从第一个障碍物开始绘制
     * 3.循环绘制，并把障碍物的x，y位置，分别保存在数组中
     * 4.障碍物逐渐上升，当障碍物超出边界时，我们删除数组中保存的
     *      第一个位置的x，但是保持原有下面已经出现过得障碍物x的位置
     *      并在最后添加新的障碍物的位置；y位置，每次都重新生成，重新
     *      保存在数组中
     * */
    private void generateBarrier(Canvas canvas) {
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(Color.DKGRAY);
        //每次都清楚Y坐标信息，因为后面会重新生成
        mBarrierYs.clear();
        //死循环，有条件退出
        for (int i = 0; ; ) {
            //i小于数组中的长度，那么取出原有的x位置信息，绘制旧障碍物;
            // 否则就随机生成新的坐标信息添加到数组中
            if (i < mBarrierXs.size()) {
                mBarrier.mPositionX = mBarrierXs.get(i);
            } else {
                mBarrier.mPositionX = PositionUtil.getRangeX(mLayoutWidth);
                mBarrierXs.add(mBarrier.mPositionX);
            }
            //障碍物的y坐标
            mBarrier.mPositionY = mBarrierStartY + mBarrierInterval * i;
            mBarrierYs.add(mBarrier.mPositionY);
            //绘制到视图外，则不再进行绘制，退出循环
            if (mBarrier.mPositionY > mLayoutHeight) {
                break;
            }
            mBarrier.drawBarrier(canvas);
            i++;
        }
    }


    private void generatePerson(Canvas canvas) {
        //如果小人在自动下落
        if (isAutoFall) {
            //自动下落绘制
//            mPerson.autoFallY();
            mFallTime += 20;
            //根据重力加速度计算小人下落的位置
            mPerson.mPersonY += mFallTime / 1000 * G;
            mPerson.draw(canvas);
        } else {
            // 获取被挡住的障碍位置
            Log.v("@time", mFallTime / 1000 + "");
            //小人被挡住，下落的时间重置
            mFallTime = 0;
            //mTouchIndex表示的是小人在视图中被阻挡的的障碍物的位置
            //如果是小于0,表示没有阻挡,
            if (mTouchIndex >= 0) {
                //设置小人被阻挡的位置，被进行绘制
                mPerson.mPersonY = mBarrierYs.get(mTouchIndex) - 2 * radius;
                mPerson.draw(canvas);
            }
        }
    }

    /**
     *碰撞检测
     */
    private void checkTouch() {
        for (int i = 0; i < mBarrierYs.size(); i++) {
            //碰撞检测
            if (isTouchBarrier(mBarrierXs.get(i), mBarrierYs.get(i))) {
                mTouchIndex = i;
                isAutoFall = false;
            }
        }
    }

    private boolean checkIsGameOver() {
        return mPerson.mPersonY < 0 || mPerson.mPersonY > mLayoutHeight - 2 * radius;
    }

    /**
     * 碰撞检测
     * @param x 障碍物x坐标
     * @param y 障碍物y坐标
     * @return
     */
    private boolean isTouchBarrier(int x, int y) {
        boolean res = false;
        int pY = mPerson.mPersonY + 2 * radius;
        //在瞬间刷新的时候，只要小人的位置和障碍的位置，差值在小人和障碍物的瞬间刷新的最大值就属于碰撞
        //比如：小人下落速度为a,障碍物上升速度为b,画面刷新时间为t,瞬间刷新，会有个最大差值，这个值就是
        //临界值
        if (Math.abs(pY - y) <= Math.abs(mBarrierMoveSpeed + Person.SPEED + mFallTime / 1000 * G)) {
            if (mPerson.mPersonX + 2 * radius >= x && mPerson.mPersonX <= x + mBarrier.getWidth()) {
                res = true;
            }
        }
        return res;
    }


    public void startGame() {
        mThread = new Thread() {
            @Override
            public void run() {
                super.run();
                while (isRunning) {
                    //开始让障碍往上面滚动,障碍物的绘制，是跟mBarrierStartY相关的
                    mBarrierStartY -= mBarrierMoveSpeed;
                    //当第一个障碍物开始消失
                    if (mBarrierStartY <= -mBarrierInterval - mBarrierHeight) {
                        mBarrierStartY = -mBarrierHeight;
                        //删除刚消失的障碍物坐标信息
                        if (mBarrierXs.size() > 0)
                            mBarrierXs.remove(0);
                        //得分++
                        mTotalScore++;
                        //小球碰撞位置--
                        mTouchIndex--;
                    }
                    //这里应该是可以直接用postInvalidate()
                    myHandler.sendEmptyMessage(0x1);
                    try {
                        //每20毫秒刷新一次界面
                        Thread.sleep(20);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        mThread.start();
    }

    private class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0x1) {
                invalidate();
            }

        }
    }

    //控制小人向左移动
    public void moveLeft() {
        int x = mPerson.mPersonX;
        int dir = x - mPersonMoveSpeed;
        if (dir < 0)
            dir = 0;
        mPerson.mPersonX = dir;
        //移动过程中，启动边界检测,设置isAutoFall为true
        checkIsOutSide(dir);
        invalidate();
    }

    /**
     * 类似moveLeft
     */
    public void moveRight() {
        int x = mPerson.mPersonX;
        int dir = x + mPersonMoveSpeed;
        if (dir > mLayoutWidth - radius * 2)
            dir = mLayoutWidth - radius * 2;
        mPerson.mPersonX = dir;
        checkIsOutSide(dir);
        invalidate();
    }

    private void checkIsOutSide(int x) {
        isAutoFall = true;
    }

    public void stop() {
        isRunning = false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //游戏正在运行，没有生成菜单
                if (isRunning)
                    break;
                //获取触摸位置信息
                float x = event.getX();
                float y = event.getY();
                //如果触摸到重启游戏的按钮，触发
                if (mRestartRectf.contains(x, y)) {
                    restartGame();
                } else if (mQuiteRectf.contains(x, y)) {//触摸到退出按钮
                    Toast.makeText(getContext(), "退出到主菜单", Toast.LENGTH_SHORT).show();
                }
                break;

        }
        return super.onTouchEvent(event);
    }

    /**
     * 重置游戏信息
     */
    private void restartGame() {
        mBarrierXs.clear();
        mBarrierYs.clear();
        mBarrierStartY = 500;
        mPerson.mPersonY = 300;
        mPerson.mPersonX = mLayoutWidth / 2;
        mTotalScore = 0;
        isAutoFall = true;
        mFallTime = 0;
        isRunning = true;
        startGame();
    }
}
