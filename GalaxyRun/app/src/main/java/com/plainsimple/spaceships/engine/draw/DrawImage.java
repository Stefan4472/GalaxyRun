package com.plainsimple.spaceships.engine.draw;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;

import com.plainsimple.spaceships.helper.BitmapCache;
import com.plainsimple.spaceships.helper.BitmapData;
import com.plainsimple.spaceships.helper.BitmapID;

/**
 * Stores instructions for drawing a Bitmap
 *
 * TODO: refactor the various DrawImage classes
 */

public class DrawImage implements DrawInstruction {

    // ID of bitmap to be drawn
    protected Bitmap bitmap;
    // x-coordinate where drawing begins on canvas
    protected float canvasX0;
    // y-coordinate where drawing begins on canvas
    protected float canvasY0;
    // rectangle specifying region of this bitmap to draw (defaults to full bitmap)
    protected Rect drawRegion;
    // degrees image will be rotated about center when drawn (default 0)
    protected float degreesRotation;
    // color filter used when drawing (default doesn't do anything)
    protected ColorMatrixColorFilter filter;
    // paint used for drawing
    private Paint paint;

    // TODO: use double
    public DrawImage(Bitmap bitmap, float canvasX0, float canvasY0) {
        this.bitmap = bitmap;
        this.canvasX0 = canvasX0;
        this.canvasY0 = canvasY0;
        paint = new Paint();
    }

    public void setCanvasX0(float canvasX0) {
        this.canvasX0 = canvasX0;
    }

    public void setCanvasY0(float canvasY0) {
        this.canvasY0 = canvasY0;
    }

    public void setDrawRegion(Rect drawRegion) {
        this.drawRegion = drawRegion;
    }

    public void setRotation(float degreesRotation) {
        this.degreesRotation = degreesRotation;
    }

    public void setFilter(ColorMatrix filter) {
        this.filter = new ColorMatrixColorFilter(filter);
    }

    @Override
    public void draw(Canvas canvas) {
        // todo: refine so data is called once and not every time. Also, set DrawRegion on init
        // set drawRegion to the full image if none was specified
        if (drawRegion == null) {
            drawRegion = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        }

        // save and rotate canvas if a rotation was specified
        if (degreesRotation != 0) {
            // save canvas so we can revert it after performing the rotation
            canvas.save();
            // rotate canvas about center of drawRegion
            canvas.rotate(degreesRotation, canvasX0 + drawRegion.width() / 2, canvasY0 + drawRegion.height() / 2);
        }

        // set color filter if one was specified
        if (filter != null) {
            paint.setColorFilter(filter);
        }

        // calculate destination coordinates
        Rect destination = new Rect(
                (int) canvasX0,
                (int) canvasY0,
                (int) canvasX0 + drawRegion.width(),
                (int) canvasY0 + drawRegion.height()
        );

        // draw command
        canvas.drawBitmap(
                bitmap,
                drawRegion,
                destination,
                paint);

        // restore canvas if it was previously rotated
        if (degreesRotation != 0) {
            canvas.restore();
        }
    }
}
