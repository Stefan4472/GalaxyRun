package plainsimple.spaceships;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.IOException;

/**
 * Created by Stefan on 8/13/2015.
 */
public class SpriteAnimation { // todo: pause (returns same frame each time) and reverse methods

    private Bitmap spriteSheet;

    // dimensions of each frame
    private int frameW;
    private int frameH;

    // dimensions of sprite sheet (images)
    private int sheetW;
    private int sheetH;

    // number of frames in loop
    private int numFrames;

    // whether or not to loop animation
    private boolean loop;

    // whether or not this animation is playing
    private boolean isPlaying = false;

    // whether or not animation has already played
    private boolean hasPlayed = false;

    // current position in array of frames
    private int frameCounter = 0;

    // number of frames to display each sprite
    private int frameSpeed;

    // counts number of frames current sprite has been shown
    private int frameSpeedCounter = 0;

    // reads in spritesheet consisting of one row of sprites
    // initializes all frames now so as to cut down on processing time later
    public SpriteAnimation(Bitmap spriteSheet, int frameW,
                           int frameH, int frameSpeed, boolean loop) {
        this.spriteSheet = spriteSheet;
        this.frameW = frameW;
        this.frameH = frameH;
        sheetW = spriteSheet.getWidth() / frameW;
        sheetH = spriteSheet.getHeight() / frameH;
        numFrames = sheetW * sheetH;
        this.frameSpeed = frameSpeed;
        this.loop = loop;
    }

    // resets fields so that nextFrame() can play animation
    public void start() { // todo: good practice is to start animation before calling nextFrame()
        isPlaying = true;
        frameCounter = 0;
        frameSpeedCounter = 0;
    }

    // stops animation
    public void stop() {
        isPlaying = false;
    }

    public void reset() {
        frameCounter = 0;
    }

    // whether animation has finished or not
    public boolean isPlaying() {
        return isPlaying;
    }

    // whether animation has played
    public boolean hasPlayed() {
        return hasPlayed;
    }

    // progresses frame counter by one
    public void incrementFrame() {
        if (frameSpeedCounter == frameSpeed) {
            frameCounter++;
            frameSpeedCounter = 0;
        } else {
            frameSpeedCounter++;
        }

        // reached end of loop
        if(!loop && frameCounter == numFrames - 1) {
            hasPlayed = true;
            isPlaying = false;
        } else if(loop && frameCounter == numFrames) {
            hasPlayed = true;
            frameCounter = 0;
        }
    }

    // returns current frame of animation
    // starts animation if it is not playing already
    public Bitmap currentFrame() {
        if (!isPlaying) {
            start();
        }
        // todo: better way to get subimage? // todo: allow for multi-row spritesheets!
        return Bitmap.createBitmap(spriteSheet, frameW * frameCounter, 0, frameW, frameH);
    }

    // returns next image in animation
    // starts animation if it is not playing already
    public Bitmap nextFrame() {
        if (!isPlaying) {
            start();
        }
        incrementFrame();
        return currentFrame();
    }
}
