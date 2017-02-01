package com.example.alexander.shapespainter.controller.commands;

import com.example.alexander.shapespainter.controller.ICommand;
import com.example.alexander.shapespainter.model.Shape;

import javax.vecmath.Vector2f;

public class MoveShapeCommand implements ICommand {
    private Shape mShape;
    private Vector2f mToPoint;
    private Vector2f mFromPoint;
    private Vector2f mDistanceFromShapeCenterToMousePos;

    public MoveShapeCommand(Shape shape, Vector2f toPoint, Vector2f fromPoint, Vector2f distanceFromShapeCenterToMousePos) {
        mShape = shape;
        mFromPoint = fromPoint;
        mToPoint = toPoint;
        mDistanceFromShapeCenterToMousePos = distanceFromShapeCenterToMousePos;
    }

    @Override
    public void execute() {
        calculateDirectionMove();
    }

    private void calculateDirectionMove() {
        if (mToPoint.x > mShape.getCenter().x && mToPoint.y > mShape.getCenter().y) {
            mShape.setCenter(new Vector2f(
                    mToPoint.x - mDistanceFromShapeCenterToMousePos.x,
                    mToPoint.y - mDistanceFromShapeCenterToMousePos.y));
        } else if (mToPoint.x < mShape.getCenter().x && mToPoint.y < mShape.getCenter().y) {
            mShape.setCenter(new Vector2f(
                    mToPoint.x + mDistanceFromShapeCenterToMousePos.x,
                    mToPoint.y + mDistanceFromShapeCenterToMousePos.y));
        } else if (mToPoint.x < mShape.getCenter().x && mToPoint.y > mShape.getCenter().y) {
            mShape.setCenter(new Vector2f(
                    mToPoint.x + mDistanceFromShapeCenterToMousePos.x,
                    mToPoint.y - mDistanceFromShapeCenterToMousePos.y));
        } else if (mToPoint.x > mShape.getCenter().x && mToPoint.y < mShape.getCenter().y) {
            mShape.setCenter(new Vector2f(
                    mToPoint.x - mDistanceFromShapeCenterToMousePos.x,
                    mToPoint.y + mDistanceFromShapeCenterToMousePos.y));
        }
    }

    @Override
    public void unExecute() {
        mShape.setCenter(mFromPoint);
    }
}
