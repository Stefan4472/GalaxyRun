package com.plainsimple.spaceships.activity;

import android.app.Activity;
import android.content.Context;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.plainsimple.spaceships.helper.Equipped;
import com.plainsimple.spaceships.helper.RawResource;
import com.plainsimple.spaceships.helper.SoundParams;
import com.plainsimple.spaceships.sprite.Spaceship;
import com.plainsimple.spaceships.util.EnumUtil;
import com.plainsimple.spaceships.view.FontTextView;
import com.plainsimple.spaceships.view.GameView;

import java.util.Hashtable;

import plainsimple.spaceships.R;

/**
 * Created by Stefan on 10/17/2015.
 */
public class GameActivity extends Activity implements SensorEventListener {

    private GameView gameView;
    private ImageButton pauseButton;
    private ImageButton muteButton;
    private ImageButton toggleBulletButton;
    private ImageButton toggleRocketButton;
    private static SoundPool soundPool;
    private static Hashtable<RawResource, Integer> soundIDs;
    private static boolean paused = false;
    private static boolean muted = false;
    private static int score = 0;
    private static float difficulty = 0;
    public static Equipped equipment;
    // points a coin is worth
    public static final int COIN_VALUE = 100;

    // sensor manager for gyroscope
    private SensorManager sensorManager;
    /* Called when activity first created */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("GameActivity", "onCreate called");
        // go full screen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        // set content view/layout to gameview layout
        setContentView(R.layout.activity_game);
        gameView = (GameView) findViewById(R.id.spaceships); // todo: what should go in onResume()?
        gameView.setKeepScreenOn(true);
        // read in current equipment
        equipment = new Equipped(this);
        // set up view elements

        pauseButton = (ImageButton) findViewById(R.id.pausebutton);
        pauseButton.setBackgroundResource(R.drawable.pause);
        muteButton = (ImageButton) findViewById(R.id.mutebutton);
        muteButton.setBackgroundResource(R.drawable.sound_on);
        toggleBulletButton = (ImageButton) findViewById(R.id.toggleBulletButton);
        toggleBulletButton.setBackgroundResource(R.drawable.bullets_button_pressed);
        toggleRocketButton = (ImageButton) findViewById(R.id.toggleRocketButton);
        toggleRocketButton.setBackgroundResource(R.drawable.rockets_button);
        // set volume control to proper stream
        setVolumeControlStream(AudioManager.STREAM_MUSIC);

        // get sensor manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
    }

    private void initMedia() {
        Log.d("Activity Class", "Creating SoundPool");
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundIDs = new Hashtable<>();
        Log.d("Activity Class", "Loading Sounds");
        soundIDs.put(RawResource.LASER, soundPool.load(this, EnumUtil.getID(RawResource.LASER), 1));
        soundIDs.put(RawResource.ROCKET, soundPool.load(this, EnumUtil.getID(RawResource.ROCKET), 1));
        soundIDs.put(RawResource.EXPLOSION, soundPool.load(this, EnumUtil.getID(RawResource.EXPLOSION), 1));
        soundIDs.put(RawResource.BUTTON_CLICKED, soundPool.load(this, EnumUtil.getID(RawResource.BUTTON_CLICKED), 1));
        soundIDs.put(RawResource.TITLE_THEME, soundPool.load(this, EnumUtil.getID(RawResource.TITLE_THEME), 1));
        Log.d("Activity Class", soundIDs.size() + " sounds loaded");
    }

    // plays a sound using the SoundPool given SoundParams
    public static void playSound(SoundParams parameters) {
        soundPool.play(soundIDs.get(parameters.getResourceID()), parameters.getLeftVolume(),
                parameters.getRightVolume(), parameters.getPriority(), parameters.getLoop(),
                parameters.getRate());
    }

    // handle user pressing pause button
    public void onPausePressed(View view) {
        if(paused) {
            pauseButton.setBackgroundResource(R.drawable.pause);
            paused = false;
            soundPool.autoResume();
        } else {
            pauseButton.setBackgroundResource(R.drawable.play);
            paused = true;
            soundPool.autoPause();
        }
    }

    public static boolean getPaused() {
        return paused;
    }

    public static int getScore() {
        return score;
    }

    public static void incrementScore(int toAdd) {
        score += toAdd;
        Log.d("GameActivity Class", "Incrementing Score by " + toAdd + " to " + score);
    }

    public static boolean isMuted() {
        return muted;
    }

    public static float getDifficulty() {
        return difficulty;
    }

    public static void incrementDifficulty(float toAdd) {
        difficulty += toAdd;
    }

    public void onMutePressed(View view) {
        if(muted) {
            muteButton.setBackgroundResource(R.drawable.sound_on);
            muted = false;
        } else {
            muteButton.setBackgroundResource(R.drawable.sound_off);
            muted = true;
        }
        AudioManager a_manager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        a_manager.setStreamMute(AudioManager.STREAM_MUSIC, muted);
    }

    public void onToggleBulletPressed(View view) {
        gameView.setFiringMode(Spaceship.BULLET_MODE);
        toggleBulletButton.setBackgroundResource(R.drawable.bullets_button_pressed);
        toggleRocketButton.setBackgroundResource(R.drawable.rockets_button);
    }

    public void onToggleRocketPressed(View view) {
        gameView.setFiringMode(Spaceship.ROCKET_MODE);
        toggleRocketButton.setBackgroundResource(R.drawable.rockets_button_pressed);
        toggleBulletButton.setBackgroundResource(R.drawable.bullets_button);
    }

    @Override
    public void onPause() {
        super.onPause(); // todo: this in last line of method?
        Log.d("GameActivity", "onPause called");
        onPausePressed(null);
        gameView.saveGameState();
        soundPool.release();
        soundPool = null;
        sensorManager.unregisterListener(this);
        // todo: persist any data
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("GameActivity", "onResume called");
        gameView.resumeGameState();
        initMedia();
        Log.d("Activity Class", "Media Initialized");
        if (sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null) {
            sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
                    SensorManager.SENSOR_DELAY_NORMAL); // todo: test sample rates // todo: works with Level 9 API +
        } else {
            Log.d("GameView Class", "No Accelerometer");
        }
        // todo: recreate any persisted data
    }

    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        Window window = getWindow();
        window.setFormat(PixelFormat.RGBA_8888);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_GYROSCOPE) {
            gameView.updateTilt(event.values[1]); // update gameView with current screen pitch // todo: this should be registered in GameActivity
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}