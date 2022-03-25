package com.plainsimple.spaceships.engine.ui;

import android.util.Log;
import android.view.MotionEvent;

import com.plainsimple.spaceships.engine.GameContext;
import com.plainsimple.spaceships.helper.Rectangle;

import java.util.Queue;

public abstract class UIElement {
    protected final GameContext gameContext;
    protected final Rectangle bounds;

    public UIElement(GameContext gameContext, Rectangle bounds) {
        this.gameContext = gameContext;
        this.bounds = bounds;
    }

    public boolean isInBounds(float x, float y) {
        return bounds.isInBounds(x, y);
    }

    public abstract boolean handleEvent(
            MotionEvent e,
            Queue<UIInputId> createdInput
    );

    public void onTouchEnter(float x, float y) {
        Log.d("UIElement", "onTouchEnter " + x + ", " + y);
    }

    public void onTouchMove(float x, float y) {
        Log.d("UIElement", "onTouchMove " + x + ", " + y);
    }

    public void onTouchLeave(float x, float y) {
        Log.d("UIElement", "onTouchLeave " + x + ", " + y);
    }
}
