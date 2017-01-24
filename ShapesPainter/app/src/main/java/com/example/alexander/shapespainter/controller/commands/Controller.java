package com.example.alexander.shapespainter.controller.commands;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.RectF;

import com.example.alexander.shapespainter.ShapesList;
import com.example.alexander.shapespainter.controller.commands.commands.AddShapeCommand;
import com.example.alexander.shapespainter.model.Shape;
import com.example.alexander.shapespainter.model.ShapeType;
import com.example.alexander.shapespainter.model.Tools;

import java.util.Map;
import java.util.Vector;

import javax.vecmath.Vector2f;

import static com.example.alexander.shapespainter.constants.ConstWorld.DEFAULT_SHIFT_POSITION_X_FOR_REDO_TOOLBAR;
import static com.example.alexander.shapespainter.constants.ConstWorld.DEFAULT_SHIFT_POSITION_X_FOR_UNDO_TOOLBAR;


public class Controller {
    private ShapesList mShapesList = new ShapesList();
    private Tools mTools;
    private Vector2f mMousePos = new Vector2f(1000, 1000);
    private PointInsideShapeManager mPointInsideShapeManager = new PointInsideShapeManager();
    private Context mContext;
    private int mScreenWidth;
    private RectF mBitmapRect = new RectF();
    private Vector<ICommand> mHistoryCommand = new  Vector<>();

    public Controller(Context context, int screenWidth) {
        mTools = new Tools(context, screenWidth);
        mScreenWidth = screenWidth;
        mContext = context;
    }

    public ShapesList getShapesDraft() {
        return mShapesList;
    }

    public ShapesList getToolsDraft() {
        return mTools.getToolsList();
    }

    public Map<Bitmap, Vector2f> getBitmapTools() {
        return mTools.getBitmapTools();
    }

    public void update() {
        updateToolbars();
        updateShapes();
    }

    private void updateToolbars() {
        updateShapesTools();
        updateBitmaps();
    }

    private void updateShapesTools() {
        for (Shape shape : mTools.getToolsList().getShapes()) {
            if (mPointInsideShapeManager.isPointInside(shape, mMousePos)) {
                switch (shape.getType()) {
                    case Ellipse:
                        mHistoryCommand.add(new AddShapeCommand(mShapesList, ShapeType.Ellipse));
                        mHistoryCommand.get(mHistoryCommand.size() - 1).execute();
                        break;
                    case  Triangle:
                        mHistoryCommand.add(new AddShapeCommand(mShapesList, ShapeType.Triangle));
                        mHistoryCommand.get(mHistoryCommand.size() - 1).execute();
                        break;
                    case Rectangle:
                        mHistoryCommand.add(new AddShapeCommand(mShapesList, ShapeType.Rectangle));
                        mHistoryCommand.get(mHistoryCommand.size() - 1).execute();
                        break;
                }
            }
        }
    }

    private void updateBitmaps() {
        Bitmap bitmap;
        Vector2f bitmapPos;
        for (Map.Entry entry : mTools.getBitmapTools().entrySet()) {
            bitmap = (Bitmap) entry.getKey();
            bitmapPos = (Vector2f) entry.getValue();
            mBitmapRect = new RectF(
                    bitmapPos.x,
                    bitmapPos.y,
                    bitmapPos.x + bitmap.getWidth(),
                    bitmapPos.y + bitmap.getHeight());
            if (bitmapPos.x == mScreenWidth - DEFAULT_SHIFT_POSITION_X_FOR_UNDO_TOOLBAR) {
                //bitmap = undo
                if (mPointInsideShapeManager.isPointInside(mBitmapRect, mMousePos, bitmap)) {
                    entry.setValue(new Vector2f(0, 0));
                }
            } else if (bitmapPos.x == mScreenWidth - DEFAULT_SHIFT_POSITION_X_FOR_REDO_TOOLBAR) {
                //bitmap = redo
                if (mPointInsideShapeManager.isPointInside(mBitmapRect, mMousePos, bitmap)) {
                    entry.setValue(new Vector2f(0, 0));
                }
            } else {
                //bitmap = trash
                if (mPointInsideShapeManager.isPointInside(mBitmapRect, mMousePos, bitmap)) {
                    entry.setValue(new Vector2f(0, 0));
                }
            }
        }
    }

    private void updateShapes() {
        for (Shape shape : mShapesList.getShapes()) {
            if (mPointInsideShapeManager.isPointInside(shape, mMousePos)) {
                shape.setCenter(mMousePos);
            }
        }
    }


    public void setMousePosition(float x, float y) {
        mMousePos.x = x;
        mMousePos.y = y;
    }
}
