package com.plainsimple.spaceships.engine.ui;

import android.util.Log;
import android.view.MotionEvent;

import com.plainsimple.spaceships.engine.GameContext;
import com.plainsimple.spaceships.engine.UpdateContext;
import com.plainsimple.spaceships.engine.draw.DrawParams;
import com.plainsimple.spaceships.helper.Rectangle;
import com.plainsimple.spaceships.util.ProtectedQueue;

import java.util.ArrayDeque;
import java.util.Queue;

public abstract class UIElement {
    protected final GameContext gameContext;
    protected final Rectangle bounds;
    protected Queue<UIInputId> createdInput;

    public UIElement(GameContext gameContext, Rectangle bounds) {
        this.gameContext = gameContext;
        this.bounds = bounds;
        createdInput = new ArrayDeque<>();
    }

    public boolean isInBounds(float x, float y) {
        return bounds.isInBounds(x, y);
    }

    public abstract void update(UpdateContext updateContext);

    public abstract void getDrawParams(ProtectedQueue<DrawParams> drawParams);

    public abstract void onTouchEnter(float x, float y);

    public abstract void onTouchMove(float x, float y);

    public abstract void onTouchLeave(float x, float y);

    public void pollAllInputs(Queue<UIInputId> input) {
        input.addAll(createdInput);
        createdInput.clear();
    }
}
