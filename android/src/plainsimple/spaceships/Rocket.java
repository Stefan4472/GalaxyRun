package plainsimple.spaceships;

import android.graphics.Bitmap;
import android.graphics.Canvas;

/**
 * Created by Stefan on 8/13/2015.
 */
public class Rocket extends Sprite {

    // todo: different rocket types (?)
    public final static int ROCKET = 1;

    // todo: figure out resource loading and whether to pass a bitmap
    public Rocket(Bitmap defaultImage, float x, float y, int rocketType) {
        super(defaultImage, x, y);
        switch(rocketType) {
            case ROCKET:
                break;
        }
        initMissile();
    }

    private void initMissile() {
        speedX = 2.0f;
        hitBox.setDimensions(9, 3);
    }

    public void updateActions() {

    }

    public void updateSpeeds() {
        if (speedX < 2.05)
            speedX += 0.001;
        else if (speedX < 2.1)
            speedX += 0.005;
        else if (speedX < 2.5)
            speedX += 0.05;
        else if (speedX < 3.0)
            speedX += 0.1;
        else if (speedX < 3.0)
            speedX += 0.15;
        else
            speedX += 0.05;
    }

    public void handleCollision(Sprite s) {
        collision = true;
        vis = false;
    }

    public void draw(Canvas canvas) {
        canvas.drawBitmap(defaultImage, x, y, null);
    }
}