package com.plainsimple.spaceships.sprite;

import android.content.Context;

import com.plainsimple.spaceships.helper.AnimCache;
import com.plainsimple.spaceships.helper.BitmapCache;
import com.plainsimple.spaceships.helper.BitmapID;
import com.plainsimple.spaceships.helper.DrawImage;
import com.plainsimple.spaceships.helper.DrawParams;
import com.plainsimple.spaceships.helper.DrawSubImage;
import com.plainsimple.spaceships.helper.FloatRect;
import com.plainsimple.spaceships.helper.SpriteAnimation;

import java.util.List;

/**
 * Rocket0 subclass. Flies in a straight line at linear
 * speed.
 */

public class Rocket0 extends Rocket {

    private static final BitmapID BITMAP_ID = BitmapID.ROCKET_0;
    private SpriteAnimation move;
    private SpriteAnimation explode;

    public Rocket0(Context context, float x, float y) {
        super(BitmapCache.getData(BITMAP_ID, context), x, y);
        speedX = 0.0067f;
        hp = 8;
        hitBox = new FloatRect(x + getWidth() * 0.7f, y - getHeight() * 0.2f, x + getWidth() * 1.5f, y + getHeight() * 1.2f);

        move = AnimCache.get(BitmapID.ROCKET_MOVE, context);
        explode = AnimCache.get(BitmapID.EXPLOSION_1, context);

        move.start();
    }

    @Override
    public void updateActions() {
        if (!isInBounds() || explode.hasPlayed()) {
            terminate = true;
        }
    }

    @Override
    public void updateSpeeds() {

    }

    @Override
    public void handleCollision(Sprite s, int damage) {
        if (!(s instanceof Spaceship)) {
            move.stop();
            explode.start();
            speedX = s.speedX;
            collides = false;
        }
    }

    @Override
    public void updateAnimations() {
        if (move.isPlaying()) {
            move.incrementFrame();
        } else if (explode.isPlaying()) {
            explode.incrementFrame();
        }
    }

    @Override
    public List<DrawParams> getDrawParams() {
        drawParams.clear();
        if (explode.isPlaying()) {
            drawParams.add(new DrawSubImage(explode.getBitmapID(), x + (explode.getFrameW() - getWidth()) / 2, y - (explode.getFrameH() - getHeight()) / 2,
                    explode.getCurrentFrameSrc())); // todo: refine
        } else if (!explode.hasPlayed()){
            drawParams.add(new DrawImage(BITMAP_ID, x, y));
            // draw moving animation behind the rocket
            drawParams.add(new DrawSubImage(move.getBitmapID(), x - move.getFrameW(), y, move.getCurrentFrameSrc()));
        }
        return drawParams;
    }
}
