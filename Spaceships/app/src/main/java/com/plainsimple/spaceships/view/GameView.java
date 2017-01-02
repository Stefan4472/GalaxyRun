package com.plainsimple.spaceships.view;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.plainsimple.spaceships.activity.GameActivity;
import com.plainsimple.spaceships.helper.*;
import com.plainsimple.spaceships.sprite.*;
import com.plainsimple.spaceships.util.FileUtil;
import com.plainsimple.spaceships.util.GameEngineUtil;
import com.plainsimple.spaceships.util.ImageUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import plainsimple.spaceships.R;

/**
 * Created by Stefan on 10/17/2015.
 */
public class GameView extends SurfaceView implements SurfaceHolder.Callback {

    private Context c;
    private SurfaceHolder mySurfaceHolder;
    // todo: make non-static
    public static int screenW;
    public static int screenH;
    private boolean running = false;
    private boolean onTitle = true;
    // whether resources have been initialized
    private boolean initialized = false;
    // name of game save to restore (null if none)
    private String restoreGameState = null;
    private GameViewThread thread;
    private Background background;
    // todo: AnimationCache using BitmapResource enums. Store num frames per animation in r/values/integers
    // stores initialized SpriteAnimations with R.drawable ID of spritesheet as key
    private HashMap<Integer, SpriteAnimation> animations;
    // dimensions of basic mapTiles
    private int tileWidth; // todo: what about bigger/smaller sprites?
    private int tileHeight;
    // space background (implements parallax scrolling)
    //private DrawBackgroundService background;
    // grid of tile ID's instructing which sprites to initialize on screen
    private byte[][] map;
    // used to generate tile-based terrain
    private TileGenerator tileGenerator;
    // number of rows of sprites that fit on screen
    private static final int ROWS = 6;
    // number of tiles elapsed since last map was generated
    private int mapTileCounter = 0;
    // keeps track of tile spaceship was on last time map was updated
    private long lastTile = 0;
    // coordinates of upper-left of "window" being shown
    private long x = 0;
    // default speed of sprites scrolling across the map (must be negative!)
    private float scrollSpeed = -0.0025f;
    // spaceship
    private Spaceship spaceship;
    // active generated obstacles
    private List<Sprite> obstacles = new ArrayList<>();
    // active generated coins
    private List<Sprite> coins = new ArrayList<>();
    // active generated aliens
    private List<Sprite> aliens = new ArrayList<>();
    // active projectiles on screen fired by spaceship
    private List<Sprite> ssProjectiles = new ArrayList<>();
    // active projectiles on screen fired by aliens
    private List<Sprite> alienProjectiles = new ArrayList<>();
    // relative speed of background scrolling to foreground scrolling
    private static final float SCROLL_SPEED_CONST = 0.4f;
    private Paint debugPaintRed = new Paint();
    private Paint debugPaintPink = new Paint();

    // health bar along bottom of screen
    private HealthBar healthBar;

    // score display in top left of screen
    private ScoreDisplay scoreDisplay;

    //private long lastSample;
    private float screenTilt;

    // sets spaceship's firing mode
    public void setFiringMode(int firingMode) {
        spaceship.setFiringMode(firingMode);
    }

    public GameView(Context context, AttributeSet attributes) {
        super(context, attributes);
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        // set up thread
        thread = new GameViewThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {

            }
        });
        setFocusable(true);
        debugPaintRed.setColor(Color.RED);
        debugPaintRed.setStyle(Paint.Style.STROKE);
        debugPaintRed.setStrokeWidth(3);
        debugPaintPink.setColor(Color.rgb(255, 105, 180));
        debugPaintPink.setStyle(Paint.Style.STROKE);
        debugPaintPink.setStrokeWidth(3);
    }

    class GameViewThread extends Thread {

        public GameViewThread(SurfaceHolder surfaceHolder, Context context, Handler handler) {
            mySurfaceHolder = surfaceHolder;
            c = context;
        }

        @Override
        public void run() {
            while (running) {
                Canvas canvas = null;
                try {
                    canvas = mySurfaceHolder.lockCanvas(null);
                    synchronized (mySurfaceHolder) {
                        draw(canvas);
                    }
                } finally {
                    if (canvas != null) {
                        mySurfaceHolder.unlockCanvasAndPost(canvas);
                    }
                }
            }
        }

        private void draw(Canvas canvas) {
            try {
                //((GameActivity) c).incrementScore(1);
                background.draw(canvas);
                if(!GameActivity.getPaused()) {
                    update();
                    background.scroll((int) (-scrollSpeed * screenW * SCROLL_SPEED_CONST * 8));
                    //background.scroll(-10);
                }
                for (Sprite o : obstacles) {
                    drawSprite(o, canvas);
                }
                for (Sprite c : coins) {
                    drawSprite(c, canvas);
                }
                for (Sprite a : aliens) {
                    drawSprite(a, canvas);
                }
                for (Sprite a : alienProjectiles) {
                    drawSprite(a, canvas);
                }
                for (Sprite s : ssProjectiles) {
                    drawSprite(s, canvas);
                }
                drawSprite(spaceship, canvas);
                healthBar.draw(canvas);
                scoreDisplay.draw(canvas);
            } catch (Exception e) {
                System.out.print("Error drawing canvas");
            }
        }

        List<DrawParams> drawParams;
        // draws sprite onto canvas using sprite drawing params and imageCache
        private void drawSprite(Sprite sprite, Canvas canvas) {
            drawParams = sprite.getDrawParams();
            for (DrawParams p : drawParams) {
                p.draw(canvas, c);
            }
            if (sprite.collides()) {
                canvas.drawRect(sprite.getHitBox().getX(), sprite.getHitBox().getY(),
                        sprite.getHitBox().getX() + sprite.getHitBox().getWidth(),
                        sprite.getHitBox().getY() + sprite.getHitBox().getHeight(), debugPaintRed);
            } else {
                canvas.drawRect(sprite.getHitBox().getX(), sprite.getHitBox().getY(),
                        sprite.getHitBox().getX() + sprite.getHitBox().getWidth(),
                        sprite.getHitBox().getY() + sprite.getHitBox().getHeight(), debugPaintPink);
            }
        }

        // updates all game logic
        // adds any new sprites and generates a new set of sprites if needed
        public void update() {
            //score += difficulty / 2; // todo: increment score based on difficulty
            GameActivity.incrementDifficulty(0.01f);
            updateMap();
            updateSpaceship();
            GameEngineUtil.getAlienBullets(alienProjectiles, aliens);
            // check collisions between user-fired projectiles and relevant sprites
            for(Sprite projectile : ssProjectiles) {
                GameEngineUtil.checkCollisions(projectile, aliens);
                GameEngineUtil.checkCollisions(projectile, obstacles);
            }
            GameEngineUtil.checkCollisions(spaceship, aliens);
            GameEngineUtil.checkCollisions(spaceship, obstacles);
            GameEngineUtil.checkCollisions(spaceship, coins);
            GameEngineUtil.checkCollisions(spaceship, alienProjectiles);
            GameEngineUtil.updateSprites(obstacles);
            GameEngineUtil.updateSprites(aliens);
            GameEngineUtil.updateSprites(coins);
            GameEngineUtil.updateSprites(ssProjectiles);
            GameEngineUtil.updateSprites(alienProjectiles);
            spaceship.updateAnimations();
            healthBar.setMovingToHealth(spaceship.getHP());
            scoreDisplay.update(GameActivity.getScore()); // todo: clumsy
        }

        // creates new sprites as specified by the map
        // generates new map chunks if needed
        private void updateMap() {
            // update x
            x += screenW * scrollSpeed;

            // check if screen has progressed to render a new tile
            if (getWTile() != lastTile) {
                Sprite to_generate;
                // add any non-empty tiles in the current column to the edge of the screen
                for (int i = 0; i < map.length; i++) {
                    if (map[i][mapTileCounter] != TileGenerator.EMPTY) {
                        to_generate = getMapTile(map[i][mapTileCounter], screenW + getWOffset(), i * tileHeight);
                        //Log.d("GameView", "Sprite initialized at " + to_generate.getX());
                        addTile(to_generate, scrollSpeed, 0); // todo: put speedX and speedY in constructor? -> Make scrollSpeed static and have sprites determine speedX and speedY on initialization?
                    }
                }
                mapTileCounter++;

                // generate more sprites
                if (mapTileCounter == map[0].length) {
                    //map = tileGenerator.generateTiles(GameActivity.getDifficulty());
                    map = tileGenerator.generateDebugTiles();
                    //updateScrollSpeed(); // todo: try disabling this
                    mapTileCounter = 0;
                }
                lastTile = getWTile();
            }
        }

        // current horizontal tile
        private long getWTile() {
            return x / tileWidth;
        }

        // number of pixels from start of current tile
        private int getWOffset() {
            return (int) x % tileWidth;
        }

        // returns sprite initialized to coordinates (x,y) given tileID
        private Sprite getMapTile(int tileID, int x, int y) throws IndexOutOfBoundsException {
            switch (tileID) {
                case TileGenerator.OBSTACLE:
                    return new Obstacle(BitmapCache.getData(BitmapResource.OBSTACLE, c), x, y); // todo: use static ImageData?
                case TileGenerator.OBSTACLE_INVIS:
                    Sprite tile = new Obstacle(BitmapCache.getData(BitmapResource.OBSTACLE, c), x, y);
                    tile.setCollides(false);
                    return tile;
                case TileGenerator.COIN: // todo: more coins = faster animation because they share the same one
                    return new Coin(BitmapCache.getData(BitmapResource.COIN, c), animations.get(R.drawable.coin_spin), animations.get(R.drawable.coin_collect), x, y);
                case TileGenerator.ALIEN_LVL1:
                    Alien1 alien_1 = new Alien1(BitmapCache.getData(BitmapResource.ALIEN, c), x, y, spaceship);
                    alien_1.injectResources(BitmapCache.getData(BitmapResource.ALIEN_BULLET, c), animations.put(R.drawable.spaceship_explode, new SpriteAnimation(BitmapCache.getData(BitmapResource.SPACESHIP_EXPLODE, c), BitmapCache.getData(BitmapResource.SPACESHIP, c).getWidth(), 5, false)));
                    return alien_1;
                default:
                    throw new IndexOutOfBoundsException("Invalid tileID (" + tileID + ")");
            }
        }

        // sets specified fields and adds sprite to arraylist
        private void addTile(Sprite s, float speedX, float speedY) {
            s.setSpeedX(speedX);
            s.setSpeedY(speedY);
            if (s instanceof Obstacle) {
                obstacles.add(s);
            } else if (s instanceof Alien) {
                aliens.add(s);
            } else if (s instanceof Coin) {
                coins.add(s);
            }
        }

        // calculates scrollspeed based on difficulty
        // difficulty starts at 0 and increases by 0.01/frame,
        // or 1 per second
        public void updateScrollSpeed() {
            scrollSpeed = (float) (-0.0025f - GameActivity.getDifficulty() / 2500.0);
            if (scrollSpeed < -0.025) { // scroll speed ceiling
                scrollSpeed = -0.025f;
            }
        }

        private void updateSpaceship() {
            spaceship.setTilt(screenTilt);
            spaceship.updateSpeeds();
            spaceship.move();
            spaceship.updateActions();
            ssProjectiles.addAll(spaceship.getAndClearProjectiles());
            
        }

        // handle user touching screen
        boolean doTouchEvent(MotionEvent motionEvent) {
            synchronized (mySurfaceHolder) {
                int event_action = motionEvent.getAction();
                float x = motionEvent.getX();
                float y = motionEvent.getY();

                switch (event_action) {
                    case MotionEvent.ACTION_DOWN:
                        if (!onTitle) {
                            spaceship.setShooting(true);
                        }
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP: // handle user clicking something
                        if (onTitle) { // change to game screen. Load resources
                            c.getResources();
                            background = new Background(screenW, screenH);
                            initImgCache();
                            initAnimations();
                            initSpaceship();
                            tileWidth = screenH / ROWS;
                            tileHeight = screenH / ROWS;
                            map = new byte[1][screenW / tileWidth];
                            tileGenerator = new TileGenerator(ROWS);
                            onTitle = false;
                            healthBar = new HealthBar(c, screenW, screenH, 30, 30);
                            scoreDisplay = new ScoreDisplay(c, 0);
                        } else {
                            spaceship.setShooting(false);
                        }
                        break;
                }
            }
            return true;
        }

        // loads in sprites, sends ID's to the proper classes, and scales them
        private void initImgCache() { // todo: determine current rocket type equipped
            // calculate scaling factor using spaceship_sprite height as a baseline
            Bitmap spaceship = BitmapFactory.decodeResource(c.getResources(), R.drawable.spaceship);
            float scalingFactor = (screenH / 6.0f) / (float) spaceship.getHeight();
            BitmapCache.setScalingFactor(scalingFactor);
        }

        private void initSpaceship() { // todo: clean up, use persistent data
            spaceship = new Spaceship(BitmapCache.getData(BitmapResource.SPACESHIP, c),
                    -BitmapCache.getData(BitmapResource.SPACESHIP, c).getWidth(),
                    screenH / 2 - BitmapCache.getData(BitmapResource.SPACESHIP, c).getHeight() / 2);
            spaceship.injectResources(animations.get(R.drawable.spaceship_move),
                    animations.get(R.drawable.spaceship_fire_rocket), new SpriteAnimation(BitmapCache.getData(BitmapResource.SPACESHIP_EXPLODE, c), BitmapCache.getData(BitmapResource.SPACESHIP, c).getWidth(), 5, false),
                    BitmapCache.getData(BitmapResource.LASER_BULLET, c), BitmapCache.getData(BitmapResource.ROCKET, c));
            spaceship.setBullets(true, BulletType.LASER);//GameActivity.equipment.getEquippedBulletType()); // todo: get equipment
            spaceship.setRockets(true, RocketType.ROCKET);//GameActivity.equipment.getEquippedRocketType());
            spaceship.setHP(30);
        }

        // initializes SpriteAnimations and stores them in animations HashMap
        private void initAnimations() { // todo: create AnimGenerator class. Creates SpriteAnimation objects on demand (except for COIN_SPIN, which all coins share). This way sprites do not share the same animation
            animations = new HashMap<>();
            animations.put(R.drawable.spaceship_move, new SpriteAnimation(BitmapCache.getData(BitmapResource.SPACESHIP_MOVE, c), BitmapCache.getData(BitmapResource.SPACESHIP, c).getWidth(), 5, true));
            animations.put(R.drawable.spaceship_fire_rocket, new SpriteAnimation(BitmapCache.getData(BitmapResource.SPACESHIP_FIRE, c), BitmapCache.getData(BitmapResource.SPACESHIP, c).getWidth(), 8, false));
            animations.put(R.drawable.spaceship_explode, new SpriteAnimation(BitmapCache.getData(BitmapResource.SPACESHIP_EXPLODE, c), BitmapCache.getData(BitmapResource.SPACESHIP, c).getWidth(), 5, false));
            animations.put(R.drawable.coin_spin, new SpriteAnimation(BitmapCache.getData(BitmapResource.COIN_SPIN, c), BitmapCache.getData(BitmapResource.COIN, c).getWidth(), 10, true));
            animations.put(R.drawable.coin_collect, new SpriteAnimation(BitmapCache.getData(BitmapResource.COIN_DISAPPEAR, c), BitmapCache.getData(BitmapResource.COIN, c).getWidth(), 5, false));
        }

        public void setSurfaceSize(int width, int height) {
            synchronized (mySurfaceHolder) {
                screenW = width;
                screenH = height;
            }
        }

        public void setRunning(boolean b) {
            running = b;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        return thread.doTouchEvent(motionEvent);
    }

    public void updateTilt(float newTilt) {
        screenTilt = newTilt;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        thread.setSurfaceSize(width, height);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        thread.setRunning(true);
        if(thread.getState() == Thread.State.NEW) {
            thread.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread.setRunning(false);
    }

    public void saveGameState() {
        GameSave save = new GameSave(c);
        save.saveAliens(aliens);
        save.saveSpaceship(spaceship);
        save.saveBullets(ssProjectiles);
        save.saveAlienBullets(alienProjectiles);
        save.saveCoins(coins);
        save.saveObstacles(obstacles);
        Log.d("GameView.java", "Saved a total of " + (aliens.size() + obstacles.size() +
                coins.size() + ssProjectiles.size() + alienProjectiles.size()) + " sprites");
    }

    public void restoreGameState() {
        Log.d("GameView.java", "Restoring game state " + System.currentTimeMillis());
        GameSave load = new GameSave(c);
        aliens = load.loadAliens();
        spaceship = load.loadSpaceship();
        obstacles = load.loadObstacles();
        coins = load.loadCoins();
        ssProjectiles = load.loadBullets();
        alienProjectiles = load.loadAlienBullets();
        Log.d("GameView.java", "Finished restoring game state " + System.currentTimeMillis());
        Log.d("GameView.java", "Restored a total of " + (aliens.size() + obstacles.size() +
                coins.size() + ssProjectiles.size() + alienProjectiles.size()) + " sprites");
    }

    public void clearGameState() {
        GameSave clear = new GameSave(c);
        clear.delete();
    }
}
