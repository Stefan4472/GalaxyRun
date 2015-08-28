import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Stefan on 8/13/2015.
 */
public class Spaceship extends Sprite {

    // arrowkey direction in x and y
    private int dx;
    private int dy;

    private final float MAX_SPEED_X = 9.0f;
    private final float MAX_SPEED_Y = 2.5f;

    private SpriteAnimation movingAnimation;
    private SpriteAnimation startMovingAnimation;
    private SpriteAnimation fireRocketAnimation;
    private SpriteAnimation explodeAnimation;

    // whether user has control over spaceship
    boolean controllable;

    // ms to wait between firing bullets
    private final int BULLET_DELAY = 100;
    private long lastFiredBullet;
    private boolean firingBullets;

    // ms to wait between firing rockets
    private final int ROCKET_DELAY = 420;
    private long lastFiredRocket;
    private boolean firingRockets;

    // keeps track of fired bullets and rockets
    private ArrayList<Sprite> projectiles;

    // default constructor
    public Spaceship(String imageName, int x, int y) {
        super(imageName, x, y);
        initCraft();
    }

    private void initCraft() {
        try { // todo: this will not catch all errors... Resources should be initialized in main or Board.java
            startMovingAnimation =
                    new SpriteAnimation("spaceship_starting_spritesheet.png", 50, 50, 1, false);
            movingAnimation = new SpriteAnimation("spaceship_moving_spritesheet.png", 50, 50, 5, true);
            fireRocketAnimation = new SpriteAnimation("spaceship_firing_spritesheet.png", 50, 50, 8, false);
        } catch(IOException e) {
            e.printStackTrace();
        }

        projectiles = new ArrayList<>();
        lastFiredBullet = 0;
        lastFiredRocket = 0;

        controllable = true;
        collides = true;
        hitBox.setDimensions(33, 28);
        hitBox.setOffsets(12, 11);
    }

    public ArrayList<Sprite> getProjectiles() { return projectiles; }

    public void setControllable(boolean controllable) { this.controllable = controllable; }

    public void updateCurrentImage() {
        currentImage = defaultImage;
        // either accelerating or breaking
        if(dx != 0) { // todo: break into simpler methods
            // sprite was previously not accelerating/breaking. Play startMovingAnimation
            if(moving == false && dx == 1) {
                currentImage = startMovingAnimation.start(); // todo: animation layering
            } else { // sprite was previously moving play startMovingAnimation if it's playing or
                // spaceship is breaking but has not stopped completely
                if(startMovingAnimation.isPlaying() || dx == -1 && speedX != 0.0) {
                    currentImage = startMovingAnimation.nextFrame();
                } else if(dx == 1){ // Play moving animation as soon as startmoving animation is over
                    currentImage = movingAnimation.nextFrame();
                }
            }
            moving = true;
        } else {
            moving = false;
        }
    }

    public void updateActions() {
        if(firingBullets == true && lastFiredBullet + BULLET_DELAY <= System.currentTimeMillis()) {
            fireBullets();
            lastFiredBullet = System.currentTimeMillis();
        }

        if(firingRockets == true && lastFiredRocket + ROCKET_DELAY <= System.currentTimeMillis()) {
            fireRockets();
            lastFiredRocket = System.currentTimeMillis();
            //currentImage = ImageUtil.layer(currentImage, fireRocketAnimation.nextFrame());
        }
        if(fireRocketAnimation.isPlaying()) { // todo: look into AlphaComposite to compose images
            //currentImage = ImageUtil.layer(currentImage, fireRocketAnimation.nextFrame()); // todo: look into storing animations as diffs and using ImageUtil layer method
        }
    }

    // fires two rockets
    public void fireRockets() {
        projectiles.add(new Rocket(x + 43, y + 15));
        projectiles.add(new Rocket(x + 43, y + 33));
    }

    // fires two bullets
    public void fireBullets() {
        projectiles.add(new Bullet(x + 43, y + 15));
        projectiles.add(new Bullet(x + 43, y + 33));
    }

    public void updateSpeeds() {
        speedY = Math.abs(speedY);
        if (speedY < MAX_SPEED_Y) {
            speedY += 0.25;
        } else if (speedY > MAX_SPEED_Y) {
            speedY = MAX_SPEED_Y;
        }
        speedY *= dy;
    }

    public void handleCollision(Sprite s) {
        vis = false;
        collision = true;
        //controllable = false;
    }

    // Sets direction of sprite based on key pressed.
    public void keyPressed(KeyEvent e) {
        if(controllable) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT) {
                dx = -1;
            } else if (key == KeyEvent.VK_RIGHT) {
                dx = 1;
            } else if (key == KeyEvent.VK_UP) {
                dy = -1;
            } else if (key == KeyEvent.VK_DOWN) {
                dy = 1;
            } else if (key == KeyEvent.VK_SPACE) {
                firingBullets = true;
            } else if (key == KeyEvent.VK_X) {
                firingRockets = true;
            }
        }
    }

    // sets movement direction to zero once key is released
    public void keyReleased(KeyEvent e) {
        if(controllable) {
            int key = e.getKeyCode();
            if (key == KeyEvent.VK_LEFT) {
                dx = 0;
            } else if (key == KeyEvent.VK_RIGHT) {
                dx = 0;
            } else if (key == KeyEvent.VK_UP) {
                dy = 0;
            } else if (key == KeyEvent.VK_DOWN) {
                dy = 0;
            } else if (key == KeyEvent.VK_SPACE) {
                firingBullets = false;
            } else if (key == KeyEvent.VK_X) {
                firingRockets = false;
            }
        }
    }
}
