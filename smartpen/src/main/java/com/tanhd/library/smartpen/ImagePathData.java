package com.tanhd.library.smartpen;

import android.graphics.Color;
import android.graphics.Path;
import android.view.View;

import com.tqltech.tqlpencomm.Dot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ImagePathData {
    public static class SerializablePath extends Path implements Serializable {

        private static final long serialVersionUID = -4914599691577104935L;

        private final ArrayList<float[]> pathPoints;

        public SerializablePath() {
            super();
            pathPoints = new ArrayList<float[]>();
        }

        public SerializablePath(SerializablePath p) {
            super(p);
            pathPoints = p.pathPoints;
        }

        public void addPathPoints(float[] points) {
            this.pathPoints.add(points);
        }

        public void clearPathPoints() {
            this.pathPoints.clear();
        }
    }
    public static class PathData {
        public int color = Color.BLACK;
        public int width = 1;
        public SerializablePath path = new SerializablePath();
    }
    private float mX, mY;
    private ArrayList<PathData> mPathList;
    private int BG_WIDTH;
    private int BG_HEIGHT;
    private final View mView;
    private boolean mIsEmpty = true;

    public ImagePathData(View v, int width, int height) {
        mView = v;
        BG_WIDTH = width;
        BG_HEIGHT = height;
        mPathList = new ArrayList<>();
        mPathList.add(new PathData());
    }

    public boolean isEmpty() {
        return mIsEmpty;
    }

    public void setSize(int width, int height) {
        BG_WIDTH = width;
        BG_HEIGHT = height;
    }

    public int getWidth() {
        return BG_WIDTH;
    }

    public int getHeight() {
        return BG_HEIGHT;
    }

    public List<PathData> getPathList() {
        return mPathList;
    }

    private SerializablePath getPath() {
        PathData pathData = mPathList.get(mPathList.size() - 1);
        return pathData.path;
    }

    private void touchStop(float x, float y) {

    }

    private void touchStart(float x, float y) {
        getPath().moveTo(x, y);
        mX = x;
        mY = y;
    }

    private void touchMove(float x, float y) {
        float dx = Math.abs(x - mX);
        float dy = Math.abs(y - mY);

        getPath().quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2);
        getPath().addPathPoints(new float[] {
                mX, mY, (x + mX) / 2, (y + mY) / 2
        });
        mX = x;
        mY = y;
    }

    public void ProcessDot(Dot dot) {
        float pointX, pointY, tmpx, tmpy;
        tmpx = dot.x;
        pointX = dot.fx;
        pointX /= 100.0;
        pointX += tmpx;

        tmpy = dot.y;
        pointY = dot.fy;
        pointY /= 100.0;
        pointY += tmpy;

        pointX *= (BG_WIDTH);          // BG_WIDTH，控件的宽
        float ax = (float) Constants.A5_WIDTH / (float)Constants.XDIST_PERUNIT;
        pointX /= ax;

        pointY *= (BG_HEIGHT);         // BG_HEIGHT，控件的高
        float ay = (float) Constants.A5_HEIGHT / (float)Constants.YDIST_PERUNIT;
        pointY /= ay;

        switch (dot.type) {
            case PEN_DOWN:
                touchStart(pointX, pointY);
                if (mView != null)
                    mView.invalidate();
                break;
            case PEN_MOVE:
                touchMove(pointX, pointY);
                if (mView != null)
                    mView.invalidate();
                break;
            case PEN_UP:
                touchStop(pointX, pointY);
                break;
        }

        mIsEmpty = false;
    }

    /**
     * Sets the pen/ink color of the signature.
     *
     * @param color a color resource
     */
    public void setPenColor(int color) {
        PathData pathData = new PathData();
        pathData.color = color;
        mPathList.add(pathData);
    }

    public void setPenSize(int size) {
        PathData pathData = new PathData();
        pathData.width = size;
        mPathList.add(pathData);
    }

    public boolean isBlank() {
        return (mPathList.size() == 0);
    }

    public void clear() {
        for (PathData pathData: mPathList) {
            pathData.path.reset();
            pathData.path.clearPathPoints();
        }
        mPathList.clear();

        if (mView != null)
            mView.invalidate();

        mIsEmpty = true;
    }
}
