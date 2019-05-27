package com.tanhd.library.smartpen;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;

import com.tqltech.tqlpencomm.Dot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Henry Dong on 2016/9/28.
 */
public class SmartPenView extends View {
    public static interface DrawPathListener {
        void onReceiveDot(MyDot dot);
        void onReceivePenHandwritingColor(int color);
    }

    public static interface ImagePathListener {
        void onPathChanged();
    }

    public static enum TYPE implements Serializable {
        DOT,
        PEN_COLOR,
    }

    public static class DATA implements Serializable {
        TYPE type;
        Object data;
    }

    private int mCanvasColor = Color.TRANSPARENT;
    private int mStrokeColor = Color.BLACK;
    private int mStrokeWidth = 3;
    private Paint mPaint;
    private List<DATA> mHistoryData = new ArrayList<>();
    private ArrayList<Dot> mDotQueue = new ArrayList<>();
    public ImagePathData mViewPath;
    private boolean mIsEmpty = true;
    private DrawPathListener mListener;
    private ImagePathListener mImageListner;

    public SmartPenView(Context context) {
        super(context);
        init();
    }

    public SmartPenView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SmartPenView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }


	public void init() {
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setDither(true);
        mPaint.setColor(mStrokeColor);
        mPaint.setAlpha(0xFF);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeJoin(Paint.Join.ROUND);
        mPaint.setStrokeCap(Paint.Cap.ROUND);
        mPaint.setStrokeWidth(mStrokeWidth);
        mViewPath = new ImagePathData(this, Constants.OUTPUT_IMAGE_WIDTH, Constants.OUTPUT_IMAGE_HEIGHT);
    }

    public void setListener(DrawPathListener listener) {
        mListener = listener;
    }

    public void setImageListener(ImagePathListener listener) {
        mImageListner = listener;
    }

    private SmartPenCallback mSmartPenCallback = new SmartPenCallback() {
        @Override
        public void onReceiveDot(Dot dot) {
            synchronized (mDotQueue) {
                mDotQueue.add(dot);
            }
            mDotHandler.sendEmptyMessage(0);
        }

        @Override
        public void onReceivePenLedConfig(byte color) {
            if (mHistoryData != null) {
                DATA data = new DATA();
                data.type = TYPE.PEN_COLOR;
                data.data = new Byte(color);
                mHistoryData.add(data);
            }
            onReceivePenHandwritingColor(color);
        }

        @Override
        public void onReceivePenHandwritingColor(byte color) {
            switch (color) {
                case 0:
                    setPenColor(Color.GRAY);
                    break;
                case 1: // blue
                    setPenColor(Color.BLUE);
                    break;
                case 2: // green
                    setPenColor(Color.rgb(0, 128, 0));
                    break;
                case 3: // cyan
                    setPenColor(Color.CYAN);
                    break;
                case 4: // red
                    setPenColor(Color.RED);
                    break;
                case 5: // magenta
                    setPenColor(Color.MAGENTA);
                    break;
                case 6: // yellow
                    setPenColor(Color.rgb(192, 192, 0));
                    break;
                case 7: // white
                    setPenColor(Color.BLACK);
                    break;
                default:
                    break;
            }
        }
    };

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //获取宽度的模式和尺寸
        int heightSize = MeasureSpec.getSize(heightMeasureSpec);
        float ratio = (float) Constants.ORIGINAL_IMAGE_WIDTH / Constants.ORIGINAL_IMAGE_HEIGHT;
        if (ratio != 0) {
            //根据宽高比ratio和模式创建一个测量值
            widthMeasureSpec = MeasureSpec.makeMeasureSpec((int) (heightSize * ratio), MeasureSpec.EXACTLY);
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mViewPath.setSize(getMeasuredWidth(), getMeasuredHeight());
    }

    private Handler mDotHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            while (true) {
                Dot dot;
                synchronized (mDotQueue) {
                    if (mDotQueue.size() == 0)
                        break;

                    dot = mDotQueue.remove(0);
                }
                mViewPath.ProcessDot(dot);
                if (mHistoryData != null) {
                    DATA data = new DATA();
                    data.type = TYPE.DOT;
                    MyDot n = new MyDot();
                    n.init(dot);
                    data.data = n;
                    mHistoryData.add(data);
                }
                if (mListener != null) {
                    MyDot n = new MyDot();
                    n.init(dot);
                    mListener.onReceiveDot(n);
                }
                mIsEmpty = false;
            }

            if (mImageListner != null)
                mImageListner.onPathChanged();

            invalidate();
        }
    };

    @Override
    public void setBackgroundColor(int color) {
        mCanvasColor = color;
    }

    public static Bitmap getBitmap(ImagePathData pathData) {
        Paint paint = new Paint();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setAlpha(0xFF);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        Bitmap bitmap = Bitmap.createBitmap(pathData.getWidth(), pathData.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        draw2(paint, canvas, pathData.getPathList());

        return bitmap;
    }

    public static Bitmap getBitmap(List list, Size size) {
        Paint paint = new Paint();
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setAlpha(0xFF);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);

        ImagePathData pathData = new ImagePathData(null, size.getWidth(), size.getHeight());
        for (int i=0; i<list.size(); i++) {
            DATA data = (DATA) list.get(i);
            switch (data.type) {
                case DOT:
                    MyDot dot = (MyDot) data.data;
                    pathData.ProcessDot(dot.getDot());
                    break;
                case PEN_COLOR:
                    Byte b = (Byte) data.data;
                    pathData.setPenColor(b.byteValue());
                    break;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(pathData.getWidth(), pathData.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);

        draw2(paint, canvas, pathData.getPathList());

        return bitmap;
    }

    private static void draw2(Paint paint, Canvas canvas, List<ImagePathData.PathData> pathDataList) {
        for (ImagePathData.PathData pathData: pathDataList) {
            paint.setColor(pathData.color);
            paint.setStrokeWidth(pathData.width);
            canvas.drawPath(pathData.path, paint);
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(mCanvasColor);
        draw2(mPaint, canvas, mViewPath.getPathList());
    }

    public void setPenColor(int color) {
        mViewPath.setPenColor(color);
        if (mListener != null)
            mListener.onReceivePenHandwritingColor(color);
    }

    private void setPenSize(int size) {
        mViewPath.setPenSize(size);
    }

    public boolean isEmpty() {
        return mIsEmpty;
    }

    public void setActive(boolean b) {
        if (b) {
            SmartPenService.getInstance().active(mSmartPenCallback);
        } else {
            SmartPenService.getInstance().unregister(mSmartPenCallback);
        }
        invalidate();
    }

    public void drawDot(MyDot dot) {
        mSmartPenCallback.onReceiveDot(dot.getDot());
    }

    public List getData() {
        return mHistoryData;
    }

    public Size getSize() {
        Size size = new Size(mViewPath.getWidth(), mViewPath.getHeight());
        return size;
    }

    public void initData(List list, Size size) {
        if (list == null)
            return;

        mHistoryData.clear();
        mViewPath.setSize(size.getWidth(), size.getHeight());
        for (int i=0; i<list.size(); i++) {
            DATA data = (DATA) list.get(i);
            mHistoryData.add(data);
            switch (data.type) {
                case DOT:
                    MyDot dot = (MyDot) data.data;
                    mViewPath.ProcessDot(dot.getDot());
                    break;
                case PEN_COLOR:
                    Byte val = (Byte) data.data;
                    setPenColor(val.byteValue());
                    break;
            }
        }

        postInvalidate();
    }

    public SmartPenCallback getSmartPenCallback() {
        return mSmartPenCallback;
    }
}
