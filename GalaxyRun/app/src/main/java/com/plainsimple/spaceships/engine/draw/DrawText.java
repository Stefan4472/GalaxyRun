package com.plainsimple.spaceships.engine.draw;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.plainsimple.spaceships.engine.ui.PauseOverlay;
import com.plainsimple.spaceships.helper.BitmapCache;

/**
 * Stores instructions for drawing text
 *
 * TODO: support using font
 */

public class DrawText implements DrawInstruction {

    // TODO: take convention of x,y as top-left?
    // text to draw
    private String text;
    // bottom-left x-coordinate
    private float x;
    // bottom-left y-coordinate
    private float y;
    // color of text to draw
    private int textColor;
    // size of text to draw
    private int textSize;
    private Typeface typeface;
    private Paint paint;

    public DrawText(String text, float x, float y, int color, int size, Typeface typeface) {
        this.text = text;
        this.x = x;
        this.y = y;
        this.textColor = color;
        this.textSize = size;
        this.typeface = typeface;

        paint = new Paint();
        paint.setColor(textColor);
        paint.setTextSize(textSize);
        paint.setTypeface(typeface);
    }

    public DrawText(String text, float x, float y, int color, int size) {
        this(text, x, y, color, size, Typeface.DEFAULT);
    }


    @Override
    public void draw(Canvas canvas) { // todo: should paint be static? should it be passed as a parameter?
        canvas.drawText(text, x, y, paint);
    }
}
