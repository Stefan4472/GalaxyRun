package com.plainsimple.spaceships.sprite;

import com.plainsimple.spaceships.engine.EventID;
import com.plainsimple.spaceships.engine.GameContext;
import com.plainsimple.spaceships.engine.UpdateContext;
import com.plainsimple.spaceships.helper.BitmapID;
import com.plainsimple.spaceships.engine.draw.DrawImage;
import com.plainsimple.spaceships.engine.draw.DrawInstruction;
import com.plainsimple.spaceships.util.ProtectedQueue;

/**
 * Created by Stefan on 8/17/2015.
 */
public class Bullet extends Sprite {

    public static final int DAMAGE = 5;
    // Bullet speed per second, as percentage of screen width
    // TODO: WOULD BE COOL TO INCREASE SPEED AS A FUNCTION OF SCROLL SPEED
    public static final double SPEED_PERCENT_PER_SEC = 0.3;


    public Bullet(GameContext gameContext, double x, double y) {
        super(gameContext, x, y, gameContext.bitmapCache.getData(BitmapID.BULLET_0));

        setHitboxOffsetX(getWidth() * 0.7);
        setHitboxOffsetY(-getHeight() * 0.2);
        setHitboxWidth(getWidth() * 0.45);
        setHitboxHeight(getHeight() * 1.4);

        setHealth(DAMAGE);
        setSpeedX(SPEED_PERCENT_PER_SEC * gameContext.gameWidthPx);
    }

    @Override
    public int getDrawLayer() {
        return 0;
    }

    @Override
    public void updateActions(UpdateContext updateContext) {
        if (!isVisibleInBounds()) {
            setCurrState(SpriteState.TERMINATED);
        }
    }

    @Override
    public void updateSpeeds(UpdateContext updateContext) {

    }

    @Override
    public void updateAnimations(UpdateContext updateContext) {

    }

    @Override
    public void handleCollision(Sprite s, int damage, UpdateContext updateContext) {
        if (!(s instanceof Spaceship)) {
            updateContext.createEvent(EventID.BULLET_COLLIDED);
            setCollidable(false);
            setCurrState(SpriteState.TERMINATED);
        }
    }

    @Override
    public void getDrawInstructions(ProtectedQueue<DrawInstruction> drawQueue) {
        drawQueue.push(new DrawImage(
                gameContext.bitmapCache.getBitmap(BitmapID.BULLET_0),
                (float) getX(),
                (float) getY()
        ));
    }
}
