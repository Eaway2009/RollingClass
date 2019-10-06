package com.tanhd.rollingclass.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.tanhd.rollingclass.R;
import com.tanhd.rollingclass.utils.SimpleUtils;


/**
 * 带角标的ImageView
 * Created by Skyler on 2017/3/4.
 */

public class CornerImageView extends  android.support.v7.widget.AppCompatImageView {
    /**
     * 角标画笔
     */
    private Paint mCirclePaint;


    /**
     * 角标数字画笔
     */
    private Paint mTextPaint;

    /**
     * 半透明遮罩画笔
     */
    private Paint mFloatPaint;

    /**
     * 角标需要显示的内容
     */
    private String cornerText = "";

    /**
     * 字体宽度
     */
    private float textWidth;

    /**
     * 字体高度
     */
    private float textHeight;

    /**
     * 字体大小
     */
    private float textSize = 50;

    /**
     * 角标位置top距离
     */
    private float cornerTop = 0;

    /**
     * 角标位置right距离
     */
    private float cornerRight = 0;

    /**
     * 空角标(只有颜色的圆点)
     */
    private boolean hiddenText = false;

    /**
     * 角标半径
     */
    private float cornerRadius;

    /**
     * 隐藏角标
     */
    private boolean hiddenCorner = false;


    /**
     * 图片角标
     */
    private Bitmap cornerBmp;

    /**
     * 加载进度
     */
    private int currentProgress = 0;

    /**
     * 角标点击范围X起点
     */
    private float clickStartX;

    /**
     * 交表点击范围Y起点
     */
    private float clickStartY;

    /**
     * 角标点击范围X结束点
     */
    private float clickEndX;

    /**
     * 交表点击范围Y结束点
     */
    private float clickEndY;

    /**
     * 角标点击事件
     */
    private OnCornerClickListener mOnCornerClickListener;

    /**
     * 默认padding
     */
    private int paddingLeft = 0;
    private int paddingTop = 0;
    private int paddingRight = 0;
    private int paddingBottom = 0;

    public CornerImageView(Context context) {
        super(context);
        init();
    }

    public CornerImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.CornerImageView);
        cornerText = array.getString(R.styleable.CornerImageView_cornerText);
        textSize = array.getDimensionPixelSize(R.styleable.CornerImageView_cornerTextSize, 0);
        cornerTop = array.getDimensionPixelOffset(R.styleable.CornerImageView_cornerTop, 0);
        cornerRight = array.getDimensionPixelOffset(R.styleable.CornerImageView_cornerRight, 0);
        cornerRadius = array.getDimensionPixelOffset(R.styleable.CornerImageView_cornerRadius, 0);
        hiddenCorner = array.getBoolean(R.styleable.CornerImageView_hiddenCorner, false);
        hiddenText = array.getBoolean(R.styleable.CornerImageView_hiddenCornerText, false);

        int loadColor = array.getResourceId(R.styleable.CornerImageView_cornerLoadColor, 0);
        if (loadColor != 0)
            mFloatPaint.setColor(ContextCompat.getColor(context, loadColor));

        if (textSize != 0)
            setCornerTextSize(textSize);

        setCornerRadius(cornerRadius);

        int textColor = array.getResourceId(R.styleable.CornerImageView_cornerTextColor, 0);
        if (textColor != 0)
            mTextPaint.setColor(ContextCompat.getColor(context, textColor));

        Drawable background = array.getDrawable(R.styleable.CornerImageView_cornerBackground);
        if (background instanceof ColorDrawable) {
            ColorDrawable colordDrawable = (ColorDrawable) background;
            mCirclePaint.setColor(colordDrawable.getColor());
        } else if (background instanceof BitmapDrawable) {
            cornerBmp = SimpleUtils.resizeImage(((BitmapDrawable) background).getBitmap(), Math.round(cornerRadius * 2), Math.round(cornerRadius * 2));
        }


    }

    private void init() {
        mFloatPaint = new Paint();
        mFloatPaint.setAntiAlias(true);
        mFloatPaint.setColor(Color.parseColor("#59000000"));


        mCirclePaint = new Paint();
        mCirclePaint.setAntiAlias(true);
        mCirclePaint.setStrokeWidth(1);
        mCirclePaint.setColor(Color.RED);

        mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(textSize);
        mTextPaint.setColor(Color.WHITE);

    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        paddingLeft = getPaddingLeft();
        paddingTop = getPaddingTop();
        paddingRight = getPaddingRight();
        paddingBottom = getPaddingBottom();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //加载进度
        if (currentProgress > 0) {
//            if (currentProgress < 0) {
//                canvas.drawRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom(), mFloatPaint);
//                String failText = "上传失败";
//                float progressWidth = mTextPaint.measureText(failText);
//                canvas.drawText(failText, (getWidth() - progressWidth) / 2, (getHeight() + textHeight) / 2, mTextPaint);
//            } else {
            canvas.drawRect(getPaddingLeft(), getPaddingTop(), getWidth() - getPaddingRight(), getHeight() - getPaddingBottom() - (getHeight() * ((float) currentProgress / 100)), mFloatPaint);
            String progress = currentProgress + "%";
            if (currentProgress < 0)
                progress = getResources().getString(R.string.lbl_upload_fail);
            float progressWidth = mTextPaint.measureText(progress);

            canvas.drawText(progress, (getWidth() - progressWidth) / 2, (getHeight() + textHeight) / 2, mTextPaint);
//            }
        }

        if (!hiddenCorner) {
            float circleCenterX = getWidth() - cornerRadius - cornerRight - (getPaddingRight() - cornerRadius);//圆心X
            float circleCenterY = cornerRadius + cornerTop + (getPaddingTop() - cornerRadius);//圆心Y

            //图片角标
            if (cornerBmp != null) {
                clickStartX = circleCenterX - cornerRadius;
                float startX = clickStartX;
                clickEndX = clickStartX + cornerRadius * 2;
                float startY = circleCenterY - cornerRadius;
                clickStartY = startY;
                clickEndY = startY + cornerRadius * 2;
                canvas.drawBitmap(cornerBmp, startX, startY, mCirclePaint);
            } else {
                if (hiddenText || TextUtils.isEmpty(cornerText)) {
                    canvas.drawCircle(circleCenterX, circleCenterY, cornerRadius, mCirclePaint);
                } else if (cornerText.length() == 1) {
                    canvas.drawCircle(circleCenterX, circleCenterY, cornerRadius, mCirclePaint);
                    canvas.drawText(cornerText, circleCenterX - textWidth / 2, circleCenterY + textHeight / 2, mTextPaint);
                } else if (cornerText.length() > 1) {
                    canvas.drawCircle(circleCenterX, circleCenterY, cornerRadius, mCirclePaint);
                    float circle2CenterX = circleCenterX - textWidth + cornerRadius;//圆心X
                    canvas.drawCircle(circle2CenterX, circleCenterY, cornerRadius, mCirclePaint);
                    //矩形填充
                    canvas.drawRect(circle2CenterX, circleCenterY - cornerRadius, circleCenterX, circleCenterY + cornerRadius, mCirclePaint);
                    canvas.drawText(cornerText, circle2CenterX - textHeight / 2, circleCenterY + textHeight / 2, mTextPaint);
                }
            }
        }


    }


    private float startX;
    private float startY;
    private boolean isClick = false;


    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                startX = event.getX();
                startY = event.getY();
                isClick = true;
                break;
            case MotionEvent.ACTION_MOVE:
                if (event.getX() - startX > 20 || event.getY() - startY > 20)
                    return super.dispatchTouchEvent(event);
                break;
            case MotionEvent.ACTION_UP:
                if (isClick && isEnabled()) {
                    if (cornerBmp == null) {
                        return super.dispatchTouchEvent(event);
                    }
                    float endX = event.getX();
                    float endY = event.getY();
                    if ((endX >= clickStartX && endX <= clickEndX) && (endY >= clickStartY && endY <= clickEndY) && mOnCornerClickListener != null)
                        mOnCornerClickListener.onCornerClickListener(this);
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }

    /**
     * 角标click 监听
     */
    public interface OnCornerClickListener {
        void onCornerClickListener(View view);

    }


    public void setOnCornerClickListener(OnCornerClickListener mOnCornerClickListener) {
        setClickable(true);
        this.mOnCornerClickListener = mOnCornerClickListener;
    }


    /**
     * 设置角标文字
     *
     * @param text 角标内容
     */
    public void setCornerText(String text) {
        //角表内容
        this.cornerText = text;
        if (!TextUtils.isEmpty(cornerText)) {
            textWidth = mTextPaint.measureText(cornerText);
            textHeight = mTextPaint.measureText(cornerText.substring(0, 1));
        }
        if (cornerRadius == 0)
            setCornerRadius(textHeight);
        hiddenCorner = false;
    }

    /**
     * 设置角标文字
     *
     * @param size 字体大小 px
     */
    public void setCornerTextSize(float size) {
        //设置画笔字号
        textSize = size;
        mTextPaint.setTextSize(textSize);
        //角标宽高
        if (!TextUtils.isEmpty(cornerText)) {
            textWidth = mTextPaint.measureText(cornerText);
            textHeight = mTextPaint.measureText(cornerText.substring(0, 1));
        }
        if (cornerRadius == 0)
            setCornerRadius(textHeight);
    }


    public void setCornerBackgroundColor(@ColorInt int color) {
        mCirclePaint.setColor(color);
        invalidate();
    }

    public void setCornerBackgroundResource(@DrawableRes int resid) {
        cornerBmp = SimpleUtils.resizeImage(BitmapFactory.decodeResource(getResources(), resid), Math.round(cornerRadius), Math.round(cornerRadius));
        invalidate();
    }


    /**
     * 设置角标文字
     *
     * @param textColor 字体颜色
     */
    public void setCornerTextColor(@ColorInt int textColor) {
        mTextPaint.setColor(textColor);
    }

    /**
     * 设置角标半径
     *
     * @param radius 半径
     */
    public void setCornerRadius(float radius) {
        this.cornerRadius = radius;
        int padding = (int) radius;
        setPadding(paddingLeft + padding, paddingTop + padding, paddingRight + padding, paddingBottom + padding);
    }


    /**
     * 隐藏
     */
    public void hiddenCorner() {
        hiddenCorner = true;
//        int padding = (int) cornerRadius;
//        setPadding(getPaddingLeft() + padding, getPaddingTop() + padding, getPaddingRight() + padding, getPaddingBottom() + padding);
        invalidate();
    }

    /**
     * 隐藏
     */
    public void showCorner() {
        hiddenCorner = false;
        int padding = (int) cornerRadius;
        setPadding(getPaddingLeft() + padding, getPaddingTop() + padding, getPaddingRight() + padding, getPaddingBottom() + padding);
        invalidate();
    }


    /**
     * @param top top距离
     */
    public void setCornerTop(float top) {
        this.cornerTop = top;
    }

    /**
     * @param right top距离
     */
    public void setCornerRight(float right) {
        this.cornerRight = right;
    }


    public void setLoadColor(int colorId) {
        mFloatPaint.setColor(ContextCompat.getColor(getContext(), colorId));
    }


    /**
     * 设置加载进度
     */
    public void progress(int progress) {
        if (progress < 100) {
            this.currentProgress = progress;
        } else {
            this.currentProgress = 0;
        }
        postInvalidate();
    }


}
