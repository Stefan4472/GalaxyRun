package com.plainsimple.spaceships.sprite;

import com.plainsimple.spaceships.helper.BitmapData;
import com.plainsimple.spaceships.helper.BitmapID;
import com.plainsimple.spaceships.helper.DrawImage;
import com.plainsimple.spaceships.helper.DrawParams;
import com.plainsimple.spaceships.helper.DrawRotatedImage;
import com.plainsimple.spaceships.helper.FloatRect;
import com.plainsimple.spaceships.view.GameView;

import java.util.List;

/**
 * An AlienBullet is a projectile fired by an Alien at a
 * certain set of coordinates (specified in the constructor).
 * The AlienBullet will determine its own speedX and speedY
 * to reach the target. It will also request to be drawn at
 * the correct angle to reach its target.
 */
public class AlienBullet extends Sprite {

    private BitmapID bitmapId;
    // angle that bullet travels
    private float fireAngle;

    public AlienBullet(BitmapData bitmapData, float x, float y, float targetX, float targetY) { // todo: damage as a parameter?
        super(x, y, bitmapData);
        bitmapId = bitmapData.getId();
        hitBox = new FloatRect(x, y, x + getWidth(), y + getHeight());
        hp = 10;

        // speedX is fixed
        speedX = -0.008f;

        // calculate fireAngle based on distance to target in x and y
        // keep in mind this gets tricky because we're in canvas coordinates
        float dist_x = x - targetX;
        float dist_y = y - targetY;

        fireAngle = (float) Math.toDegrees(Math.atan(dist_y / dist_x));

        // calculate speedY using relative screen dimensions
        float frames_to_impact = Math.abs(dist_x / GameView.screenW / speedX);
        speedY = -dist_y / GameView.playScreenH / frames_to_impact;

        // cap speedY if abs. value greater than 0.012f
        speedY = (Math.abs(speedY) > 0.012f ? Math.signum(speedY) * 0.012f : speedY);
    }

    @Override
    public void updateActions() {
        if (!isInBounds()) {
            terminate = true;
        }
    }

    @Override
    public void updateSpeeds() {

    }

    @Override
    public void updateAnimations() {

    }

    @Override
    public void handleCollision(Sprite s, int damage) {
        collides = false;
        terminate = true;
    }

    // DrawParam used to draw the AlienBullet
    private DrawImage DRAW_BULLET = new DrawImage(bitmapId);

    @Override
    public List<DrawParams> getDrawParams() {
        DRAW_BULLET.setCanvasX0(x);
        DRAW_BULLET.setCanvasY0(y);
        DRAW_BULLET.setRotation((int) fireAngle);
        drawParams.set(0, DRAW_BULLET);
        return drawParams;
    }
}
