package com.example.alexander.shapespainter.controller;

import android.content.Context;
import android.widget.Toast;

import com.example.alexander.shapespainter.controller.commands.AddShapeCommand;
import com.example.alexander.shapespainter.controller.commands.MoveShapeCommand;
import com.example.alexander.shapespainter.controller.commands.RemoveShapeCommand;
import com.example.alexander.shapespainter.controller.commands.ResizeShapeCommand;
import com.example.alexander.shapespainter.model.SelectShapeDiagram;
import com.example.alexander.shapespainter.model.Shape;
import com.example.alexander.shapespainter.model.ShapeDiagram;
import com.example.alexander.shapespainter.model.ShapeType;
import com.example.alexander.shapespainter.model.ShapesList;
import com.example.alexander.shapespainter.utils.FileSystem;

import java.io.IOException;

import javax.vecmath.Vector2f;

import static com.example.alexander.shapespainter.constants.Constant.DEFAULT_RADIUS_DRAG_POINT;

public class Controller {
    private ShapesList mShapesList = new ShapesList();
    private Context mContext;
    private SelectShapeDiagram mSelectDiagramShape = new SelectShapeDiagram();
    private DragType mDragType = null;
    private MouseActionType mMouseActionType = MouseActionType.None;
    private CommandStack mCommandStack = new CommandStack();
    private ResizeShapeCommand mCommandEnderResize;

    private Vector2f mStartPositionClickMouse = new Vector2f();
    private Vector2f mStartSizeShapeThenClickMouse = new Vector2f();
    private Vector2f mStartCenterShapeThenClickMouse = new Vector2f();
    private Vector2f mDistanceFromShapeCenterToMousePos = new Vector2f();

    public Controller(Context context) {
        mContext = context;
    }

    public void readFileWithStateShape() {
        try {
            FileSystem.readFileWithStateShapes(mShapesList, mContext);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public SelectShapeDiagram getSelectDiagramShape() {
        return mSelectDiagramShape;
    }

    public ShapesList getShapesDraft() {
        return mShapesList;
    }

    public void addEllipse() {
        mCommandStack.add(new AddShapeCommand(mShapesList, ShapeType.Ellipse));
        mSelectDiagramShape.setShape(mShapesList.getShapes().get(mShapesList.getShapes().size() - 1));
    }

    public void addTriangle() {
        mCommandStack.add(new AddShapeCommand(mShapesList, ShapeType.Triangle));
        mSelectDiagramShape.setShape(mShapesList.getShapes().get(mShapesList.getShapes().size() - 1));
    }

    public void addRectangle() {
        mCommandStack.add(new AddShapeCommand(mShapesList, ShapeType.Rectangle));
        mSelectDiagramShape.setShape(mShapesList.getShapes().get(mShapesList.getShapes().size() - 1));
    }

    public void undoCommand() {//назад
        if (mCommandStack.undoEnabled()) {
            mCommandStack.undo();
        } else {
            setMessage("отменить нельзя");
        }
        mSelectDiagramShape.setShape(null);
    }

    public void redoCommand() {//вперед
        if (mCommandStack.redoEnabled()) {
            mCommandStack.redo();
        } else {
            setMessage("вернуть нельзя");
        }
        mSelectDiagramShape.setShape(null);
    }

    public void deleteSelectedShape() {
        if (mSelectDiagramShape.getShape() != null) {
            selectShapeClear();
        } else {
            setMessage("чтобы удалить фигуру, нужно ее сначала выделить");
        }
    }

    public void updateShapes(Vector2f mousePos) {
        for (Shape shape : mShapesList.getShapes()) {
            switch (mMouseActionType) {
                case Down:
                    mouseDown(shape, mousePos);
                    updateResizeShape(mousePos);
                    break;
                case Move:
                    mouseMoved(shape, mousePos);
                    updateResizeShape(mousePos);
                    break;
                case Up:
                    updateResizeShape(mousePos);
                    mouseUp(mousePos);
                    break;
            }

        }
    }

    public void setMouseMotionType(MouseActionType mouseActionType) {
        mMouseActionType = mouseActionType;
    }

    private void selectShapeClear() {
        mCommandStack.add(new RemoveShapeCommand(mShapesList, mSelectDiagramShape.getShape()));
        mSelectDiagramShape.setShape(null);
        mCommandEnderResize = null;
    }

    private void mouseDown(Shape shape, Vector2f mousePos) {
        if (PointInsideShapeManager.isPointInside(shape, mousePos) && !calculateDragType(mousePos)) {
            mCommandEnderResize = null;
            mSelectDiagramShape.setShape(shape);
            mDragType = null;
            mStartPositionClickMouse = mousePos;
            mDistanceFromShapeCenterToMousePos.set(
                    Math.abs(mousePos.x - shape.getCenter().x),
                    Math.abs(mousePos.y - shape.getCenter().y));
        }
        if (mSelectDiagramShape.getShape() != null
                && !PointInsideShapeManager.isPointInside(mSelectDiagramShape.getShape(), mousePos)
                && mMouseActionType == MouseActionType.Down && !calculateDragType(mousePos)) {
            mDragType = null;
            mSelectDiagramShape.setShape(null);
        }
        if (mSelectDiagramShape.getShape() != null) {
            mStartCenterShapeThenClickMouse = mSelectDiagramShape.getShape().getCenter();
            mStartSizeShapeThenClickMouse = mSelectDiagramShape.getShape().getSize();
        }
    }

    private void mouseMoved(Shape shape, Vector2f mousePos) {
        if (shape == mSelectDiagramShape.getShape() && mDragType == null) {
            MoveShapeCommand moveShapeCommand = new MoveShapeCommand(
                    mSelectDiagramShape.getShape(),
                    mousePos,
                    mStartPositionClickMouse,
                    mDistanceFromShapeCenterToMousePos);
            moveShapeCommand.execute();
        }
    }

    private void mouseUp(Vector2f mousePos) {
        if (mSelectDiagramShape.getShape() != null) {
            if (mCommandEnderResize != null) {
                mCommandStack.add(mCommandEnderResize);
            }
        }
        mDragType = null;
    }

    private void updateResizeShape(Vector2f mousePos) {
        Shape selectShape = mSelectDiagramShape.getShape();
        if (selectShape != null && mMouseActionType != MouseActionType.Move) {
            calculateDragType(mousePos);
        }
        if (mDragType != null) {
            mCommandEnderResize = new ResizeShapeCommand(selectShape,
                    mStartSizeShapeThenClickMouse,
                    mStartCenterShapeThenClickMouse,
                    mousePos,
                    mDragType);
            mCommandEnderResize.execute();
        }
        if (mDragType == null && mMouseActionType == MouseActionType.Up && mSelectDiagramShape.getShape() != null) {
            mCommandStack.add(new MoveShapeCommand(mSelectDiagramShape.getShape(), mousePos, mStartPositionClickMouse, mDistanceFromShapeCenterToMousePos));
        }
    }

    private void setMessage(String message) {
        Toast toast = Toast.makeText(mContext.getApplicationContext(),
                message, Toast.LENGTH_SHORT);
        toast.show();
    }

    private boolean calculateDragType(Vector2f mousePos) {
        int sizeInvisibleRadiusForUsability = 15;
        if (mSelectDiagramShape.getShape() != null) {
            ShapeDiagram shapeDiagram = mSelectDiagramShape.getShape().getDiagram();
            if (Math.pow((mousePos.x - shapeDiagram.getLeft()), 2)
                    / Math.pow(DEFAULT_RADIUS_DRAG_POINT + sizeInvisibleRadiusForUsability, 2)
                    + Math.pow((mousePos.y - shapeDiagram.getTop()), 2)
                    / Math.pow(DEFAULT_RADIUS_DRAG_POINT + sizeInvisibleRadiusForUsability, 2) <= 1) {
                mDragType = DragType.LeftTop;
                return true;
            }
            if (Math.pow((mousePos.x - shapeDiagram.getRight()), 2)
                    / Math.pow(DEFAULT_RADIUS_DRAG_POINT + sizeInvisibleRadiusForUsability, 2)
                    + Math.pow((mousePos.y - shapeDiagram.getTop()), 2)
                    / Math.pow(DEFAULT_RADIUS_DRAG_POINT + sizeInvisibleRadiusForUsability, 2) <= 1) {
                mDragType = DragType.RightTop;
                return true;
            }
            if (Math.pow((mousePos.x - shapeDiagram.getLeft()), 2)
                    / Math.pow(DEFAULT_RADIUS_DRAG_POINT + sizeInvisibleRadiusForUsability, 2)
                    + Math.pow((mousePos.y - shapeDiagram.getBottom()), 2)
                    / Math.pow(DEFAULT_RADIUS_DRAG_POINT + sizeInvisibleRadiusForUsability, 2) <= 1) {
                mDragType = DragType.LeftBottom;
                return true;
            }
            if (Math.pow((mousePos.x - shapeDiagram.getRight()), 2)
                    / Math.pow(DEFAULT_RADIUS_DRAG_POINT + sizeInvisibleRadiusForUsability, 2)
                    + Math.pow((mousePos.y - shapeDiagram.getBottom()), 2)
                    / Math.pow(DEFAULT_RADIUS_DRAG_POINT + sizeInvisibleRadiusForUsability, 2) <= 1) {
                mDragType = DragType.RightBottom;
                return true;
            }
        }
        return false;
    }

    public void saveStateShape() {
        FileSystem.saveFileWithStateShapes(getShapesDraft(), mContext);
    }
}
