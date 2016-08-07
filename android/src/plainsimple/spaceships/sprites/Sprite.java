package plainsimple.spaceships.sprites;

import android.graphics.Rect;
import plainsimple.spaceships.util.BitmapData;
import plainsimple.spaceships.util.DrawParams;
import plainsimple.spaceships.util.Point2D;
import plainsimple.spaceships.view.GameView;

import java.util.*;

/**
 * Created by Stefan on 8/12/2015.
 */
public abstract class Sprite { // todo: figure out public vs. protected

    // coordinates of sprite
    protected int x;
    protected int y;

    // intended movement in x and y directions each frame
    protected double speedX;
    protected double speedY;

    // damage sprite can withstand and damage it inflicts on collision
    protected int hp;
    protected int damage;

    // whether or not sprite is visible on screen
    protected boolean vis;
    // whether or not sprite is in screen bounds
    protected boolean inBounds;
    // whether or not sprite can collide with other sprites
    protected boolean collides; // todo: flags all in one bitwise operator?
    // whether or not sprite is currently moving
    protected boolean moving; // todo: remove?
    // whether or not sprite should be removed from game
    protected boolean terminate;

    // hitbox for collision detection todo: complex shapes
    protected Rect hitBox;

    // data concerning sprite's default Bitmap
    protected BitmapData bitmapData;

    // random number generator
    protected Random random;

    public Sprite(BitmapData bitmapData, int x, int y) {
        this.bitmapData = bitmapData;
        this.x = x;
        this.y = y;
        initSprite();
    }

    private void initSprite() {
        vis = true;
        //inBounds = true; // todo: shouldn't be set automatically
        moving = true;
        collides = true;
        terminate = false;
        speedX = 0.0f;
        speedY = 0.0f;
        hitBox = new Rect();
        random = new Random();
        damage = 0;
        hp = 0;
    }

    // update/handle any actions sprite takes
    public abstract void updateActions();

    // update speedX and speedY
    public abstract void updateSpeeds();

    // start/stop/update any animations the sprite may play
    public abstract void updateAnimations();

    // handles collision with another sprite
    public abstract void handleCollision(Sprite s);

    // returns an ArrayList specifying Bitmaps to be drawn for the sprite
    public abstract ArrayList<DrawParams> getDrawParams();

    // moves sprite using speedX and speedY, updates hitbox,
    // and checks if sprite is still visible
    public void move() {
        x += (int) (GameView.screenW * speedX);
        y += (int) (GameView.screenH * speedY);
        hitBox.offset((int) (GameView.screenW * speedX), (int) (GameView.screenH * speedY));
        // keep in mind sprites are generated past the screen
        if (x > GameView.screenW + bitmapData.getWidth() || x < -bitmapData.getWidth()) { // todo: set terminate? Do this calculation in the sprite's own move() method?
            inBounds = false;
        } else if (y > GameView.screenH + bitmapData.getHeight() || y < -bitmapData.getHeight()) { // todo: bounce off edge of screen
            inBounds = false; // todo: use hitbox to judge if it is in bounds or not?
        } else {
            inBounds = true;
        }
    }

    // returns whether hitbox of this sprite intersects hitbox of specified sprite // todo: some methods could be made static or put in a SpriteUtil or GameEngineUtil class
    public boolean collidesWith(Sprite s) {
        if (!collides || !s.collides) {
            return false;
        } else {
            return Rect.intersects(hitBox, s.hitBox);
        }
    }

    // returns distance between centers of sprite hitboxes, as portion of screen width
    public double distanceTo(Sprite s) {
        return Math.sqrt(Math.pow((s.getHitboxCenter().getX() - getHitboxCenter().getX()), 2)
                + Math.pow((s.getHitboxCenter().getY() - getHitboxCenter().getY()), 2)) / GameView.screenW;
    }

    // returns coordinates of center of sprite's hitbox
    // as a Point2D object
    public Point2D getHitboxCenter() {
        return new Point2D(hitBox.left + hitBox.width() / 2, hitBox.right + hitBox.height() / 2);
    }

    public boolean getP(double probability) {
        return random.nextInt(1_000) + 1 <= probability * 1_000;
    } //todo: make static

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    // returns width of bitmap
    public int getWidth() {
        return bitmapData.getWidth();
    }

    // returns height of bitmap
    public int getHeight() {
        return bitmapData.getHeight();
    }

    // changes x-coordinate and updates hitbox as well
    public void setX(int x) {
        int dx = x - this.x;
        this.x = x;
        hitBox.offset(dx, 0);
    }

    // changes y-coordinate and updates hitbox as well
    public void setY(int y) { // todo: see if hitBox change is necessary - shouldn't be or should be in a separate method
        int dy = y - this.y;
        this.y = y;
        hitBox.offset(0, dy);
    }

    public BitmapData getBitmapData() {
        return bitmapData;
    }

    public void setBitmapData(BitmapData bitmapData) {
        this.bitmapData = bitmapData;
    }

    public void setSpeedX(double speedX) {
        this.speedX = speedX;
    }

    public void setSpeedY(double speedY) {
        this.speedY = speedY;
    }

    public Rect getHitBox() {
        return hitBox;
    }

    public boolean isVisible() {
        return vis;
    }

    public void setVisible(Boolean visible) {
        vis = visible;
    }

    public boolean isInBounds() {
        return inBounds;
    }

    public boolean collides() {
        return collides;
    }

    public void setCollides(boolean collides) {
        this.collides = collides;
    }

    public boolean terminate() {
        return terminate;
    }

    public void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

    public int getDamage() {
        return damage;
    }

    public void setDamage(int damage) {
        this.damage = damage;
    }

    public int getHP() {
        return hp;
    }

    public void setHP(int hp) {
        this.hp = hp;
    }
}
