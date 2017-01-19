package com.example.alexander.shapespainter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.view.SurfaceHolder;

import java.nio.Buffer;
import java.nio.ByteBuffer;

/**
 * Created by Alexander on 18.01.2017.
 */

public class PainterThread extends Thread {
    public static final int SLEEP = 0;
    public static final int READY = 1;
    public static final int SETUP = 2;

    private SurfaceHolder mSurfaceHolder;
    private boolean mIsActive = false;
    private Bitmap mBitmap;
    private int mCanvasBgColor = Color.WHITE;
    private Canvas mCanvas;
    private State mState;
    private int mStatus;
    private  Paint paint = new Paint();

    public PainterThread(SurfaceHolder surfaceHolder) {

       paint.setColor(Color.BLACK);

        mSurfaceHolder = surfaceHolder;
    }

    public void setRunning(boolean run) {
        mIsActive = run;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void restoreBitmap(Bitmap bitmap, Matrix matrix) {
        mCanvas.drawBitmap(bitmap, matrix, new Paint(Paint.FILTER_BITMAP_FLAG));
        mState.undoBuffer = saveBuffer();
    }

    public void freeze() {
        mStatus = SLEEP;
    }

    public void activate() {
        mStatus = READY;
    }

    public void setup() {
        mStatus = SETUP;
    }

    public boolean isFreeze() {
        return (mStatus == SLEEP);
    }

    public boolean isSetup() {
        return (mStatus == SETUP);
    }

    public boolean isReady() {
        return (mStatus == READY);
    }

    public boolean isRun() {
        return mIsActive;
    }

    @Override
    public  void run() {
        Canvas canvas;
        while (mIsActive) {
            canvas = null;
            try {
                // получаем объект Canvas и выполняем отрисовку
                canvas = mSurfaceHolder.lockCanvas(null);
                synchronized (mSurfaceHolder) {
                    switch (mStatus) {
                        case READY: {
                            if (canvas != null) {// init
                                canvas.drawBitmap(mBitmap, 0, 0, null);
                                canvas.drawText("hey", 10, 25, paint);
                            }
                            break;
                        }
                        case SETUP: {//for shapes drawing
                            if (canvas != null) {
                                canvas.drawBitmap(mBitmap, 0, 0, null);
                            }
                            break;
                        }
                    }
                }
            }
            finally {
                if (canvas != null) {
                    // отрисовка выполнена. выводим результат на экран
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
                if (isFreeze()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }


    public void setBitmap(Bitmap bitmap, boolean clear) {
        mBitmap = bitmap;
        if (clear) {
            mBitmap.eraseColor(mCanvasBgColor);
        }
        mCanvas = new Canvas(mBitmap);
    }

    private byte[] saveBuffer() {
        byte[] buffer = new byte[mBitmap.getRowBytes() * mBitmap.getHeight()];
        Buffer byteBuffer = ByteBuffer.wrap(buffer);
        mBitmap.copyPixelsToBuffer(byteBuffer);
        return buffer;
    }

    private void restoreBuffer(byte[] buffer) {
        Buffer byteBuffer = ByteBuffer.wrap(buffer);
        mBitmap.copyPixelsFromBuffer(byteBuffer);
    }

    public void clearBitmap() {
        mBitmap.eraseColor(mCanvasBgColor);
        mState.undoBuffer = null;
        mState.redoBuffer = null;
    }

    public static class State {
        public byte[] undoBuffer = null;
        public byte[] redoBuffer = null;
        public boolean isUndo = false;
    }
}