package com.plainsimple.spaceships.sprite;

import com.plainsimple.spaceships.engine.AnimID;
import com.plainsimple.spaceships.engine.EventID;
import com.plainsimple.spaceships.engine.GameContext;
import com.plainsimple.spaceships.engine.GameEngine;
import com.plainsimple.spaceships.engine.UpdateContext;
import com.plainsimple.spaceships.helper.ColorMatrixAnimator;
import com.plainsimple.spaceships.helper.BitmapID;
import com.plainsimple.spaceships.helper.DrawImage;
import com.plainsimple.spaceships.helper.DrawParams;
import com.plainsimple.spaceships.helper.SoundID;
import com.plainsimple.spaceships.helper.SpriteAnimation;
import com.plainsimple.spaceships.util.ProtectedQueue;

import static com.plainsimple.spaceships.sprite.Spaceship.Direction.DOWN;
import static com.plainsimple.spaceships.sprite.Spaceship.Direction.UP;

/**
 * Created by Stefan on 8/13/2015.
 */
public class Spaceship extends Sprite {

    // SpriteAnimations used
    private SpriteAnimation moveAnim;
    private SpriteAnimation explodeAnim;

    // DrawParam objects that specify how to draw the Spaceship
    private DrawImage DRAW_SHIP;
    private DrawImage DRAW_EXHAUST;
    private DrawImage DRAW_EXPLODE;

    // used to create the spaceship flash animation when hit
    private ColorMatrixAnimator colorMatrixAnimator = new ColorMatrixAnimator(3, 4, 2);

    // whether user has control over spaceship
    private boolean isControllable;

    // number of frames elapsed since cannon was last fired
    private int lastFiredCannon;
    // Is the player in the processes of shooting the cannons?
    private boolean isShooting;

    // available directions Spaceship can moveAnim in (up, down, or continue straight horizontally)
    public enum Direction {
        UP,
        DOWN,
        NONE
    }

    // Spaceship's current direction
    private Direction direction;

    // SoundIDs Spaceship uses
    private static final SoundID BULLET_SOUND = SoundID.LASER;
    private static final SoundID EXPLODE_SOUND = SoundID.EXPLOSION;

    public Spaceship(
            int spriteId,
            double x,
            double y,
            GameContext gameContext
    ) {
        super(spriteId, SpriteType.SPACESHIP, x, y, BitmapID.SPACESHIP, gameContext);
        this.gameContext = gameContext;

        // Position hitbox
        setHitboxOffsetX(getWidth() * 0.17);
        setHitboxOffsetY(getHeight() * 0.2f);

        // Load animations from AnimCache
        moveAnim = gameContext.getAnimFactory().get(AnimID.SPACESHIP_MOVE);
        explodeAnim = gameContext.getAnimFactory().get(AnimID.SPACESHIP_EXPLODE);

        // Init DrawParams
        DRAW_SHIP = new DrawImage(BitmapID.SPACESHIP);
        DRAW_EXHAUST = new DrawImage(moveAnim.getBitmapID());
        DRAW_EXPLODE = new DrawImage(explodeAnim.getBitmapID());

        // Call initialization logic
        reset();
    }

    public void reset() {
        setHealth(GameEngine.STARTING_PLAYER_HEALTH);
        setCurrState(SpriteState.ALIVE);
        setCollidable(true);
        setControllable(false);  // TODO: REMOVE?
        setSpeedX(0.0);
        setSpeedY(0.0);

        moveAnim.reset();
        explodeAnim.reset();

        moveAnim.start();
        lastFiredCannon = Bullet.DELAY_FRAMES;
    }

    @Override
    public void updateActions(UpdateContext updateContext) {
        lastFiredCannon++;

        // fires cannons if in correct FireMode, has waited long enough, and is still alive
        if (canShoot()) {
            fireCannons(updateContext);
            lastFiredCannon = 0;
        }

        // Checks if explosion has played, in which case terminate should be set to true and onInvisible() called
        if (getCurrState() == SpriteState.DEAD && explodeAnim.hasPlayed()) {
            setCollidable(false);
            setVisible(false);
            setCurrState(SpriteState.TERMINATED);
            updateContext.createEvent(EventID.SPACESHIP_INVISIBLE);
        }
    }

    private boolean canShoot() {
        return isControllable &&
                isShooting &&
                lastFiredCannon >= Bullet.DELAY_FRAMES &&
                getCurrState() == SpriteState.ALIVE;
    }

    private void fireCannons(UpdateContext updateContext) {
        // TODO: DON'T WE NEED TO CHECK THAT WE CAN FIRE?
        updateContext.registerChild(gameContext.createBullet(
                getX() + getWidth() * 0.78f,
                getY() + 0.28f * getHeight()
        ));
        updateContext.registerChild(gameContext.createBullet(
                getX() + getWidth() * 0.78f,
                getY() + 0.66f * getHeight()
        ));
        updateContext.createSound(BULLET_SOUND);
        updateContext.createEvent(EventID.BULLET_FIRED);
        updateContext.createEvent(EventID.BULLET_FIRED);
    }

    // updates the direction the Spaceship is moving in
    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public void setShooting(boolean isShooting) {
        this.isShooting = isShooting;
    }

    @Override
    public void updateSpeeds(UpdateContext updateContext) {
        if (isControllable) {
            if (direction == UP) {
                setSpeedY(-0.015 * gameContext.getGameHeightPx());
            } else if (direction== DOWN){
                setSpeedY(0.015 * gameContext.getGameWidthPx());
            } else {
                // Slow down
                setSpeedY(getSpeedY() / 1.7);
            }
        }
    }

    @Override
    public void move(UpdateContext updateContext) {
        super.move(updateContext);
        // prevent spaceship from going off-screen
        if (getY() < 0) {
            setY(0);
        } else if (getY() > gameContext.getGameHeightPx() - getHeight()) {
            setY(gameContext.getGameHeightPx() - getHeight());
        }
    }

    @Override
    public void updateAnimations(UpdateContext updateContext) {
        // Update ColorMatrixAnimator  TODO: USE TIME
        colorMatrixAnimator.update();

        // Update animations
        if (moveAnim.isPlaying()) {
            moveAnim.update(updateContext.getGameTime().getMsSincePrevUpdate());
        }
        if (explodeAnim.isPlaying()) {
            explodeAnim.update(updateContext.getGameTime().getMsSincePrevUpdate());
        }
    }

    @Override
    public void handleCollision(
            Sprite otherSprite,
            int damage,
            UpdateContext updateContext
    ) {
        takeDamage(damage, updateContext);

        // Handle coin collision
        if (otherSprite.getSpriteType() == SpriteType.COIN) {
            updateContext.createEvent(EventID.COIN_COLLECTED);
            updateContext.createSound(SoundID.COIN_COLLECTED);
        }

        // Trigger flash if we are alive and took damage
        if (getCurrState() == SpriteState.ALIVE && damage > 0) {
            updateContext.createEvent(EventID.SPACESHIP_DAMAGED);
            colorMatrixAnimator.flash();
        }
    }

    @Override
    public void takeDamage(int damage, UpdateContext updateContext) {
        updateContext.createEvent(EventID.SPACESHIP_DAMAGED);
        super.takeDamage(damage, updateContext);
    }

    @Override
    public void die(UpdateContext updateContext) {
        updateContext.createSound(EXPLODE_SOUND);
        explodeAnim.start();
        updateContext.createEvent(EventID.SPACESHIP_KILLED);
        setCurrState(SpriteState.DEAD);
    }

    @Override
    public void getDrawParams(ProtectedQueue<DrawParams> drawQueue) {
        if (!explodeAnim.hasPlayed()) {
            // Draw the Spaceship
            DRAW_SHIP.setCanvasX0((float) getX());
            DRAW_SHIP.setCanvasY0((float) getY());
            DRAW_SHIP.setFilter(colorMatrixAnimator.getMatrix());
            drawQueue.push(DRAW_SHIP);

            // Draw the moving animation behind it
            DRAW_EXHAUST.setCanvasX0((float) getX());
            DRAW_EXHAUST.setCanvasY0((float) getY());
            DRAW_EXHAUST.setDrawRegion(moveAnim.getCurrentFrameSrc());
            DRAW_EXHAUST.setFilter(colorMatrixAnimator.getMatrix());
            drawQueue.push(DRAW_EXHAUST);

            // Draw the explosion animation if it is playing
            if (explodeAnim.isPlaying()) {
                DRAW_EXPLODE.setCanvasX0((float) getX());
                DRAW_EXPLODE.setCanvasY0((float) getY());
                DRAW_EXPLODE.setDrawRegion(explodeAnim.getCurrentFrameSrc());
                drawQueue.push(DRAW_EXPLODE);
            }
        }
    }

    public void setControllable(boolean controllable) {
        this.isControllable = controllable;
    }
}