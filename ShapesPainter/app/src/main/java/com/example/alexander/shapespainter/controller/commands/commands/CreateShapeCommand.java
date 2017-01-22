package com.example.alexander.shapespainter.controller.commands.commands;

import com.example.alexander.shapespainter.PictureDraft;
import com.example.alexander.shapespainter.controller.commands.ICommand;
import com.example.alexander.shapespainter.model.Shape;

public class CreateShapeCommand implements ICommand {
    private PictureDraft mPictureDraft;
    private Shape mShape;

    public CreateShapeCommand(PictureDraft pictureDraft, Shape shape) {
        mPictureDraft = pictureDraft;
        mShape = shape;
    }

    @Override
    public void execute() {
        mPictureDraft.addShape(mShape);
    }
}