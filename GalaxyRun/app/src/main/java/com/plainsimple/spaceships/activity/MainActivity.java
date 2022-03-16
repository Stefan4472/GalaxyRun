package com.plainsimple.spaceships.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.plainsimple.spaceships.view.FontButton;
import com.plainsimple.spaceships.view.FontTextView;

import plainsimple.spaceships.R;

public class MainActivity extends Activity {

    // TODO: play a song? play an animation?
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        setContentView(R.layout.mainscreen_layout);

        // Animate the Title to "inflate"/zoom in on start
        FontTextView title = findViewById(R.id.title);
        AnimatorSet title_zoom = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.zoom_in_out);
        title_zoom.setTarget(title);
        title_zoom.start();

        // Animate the PlayButton to fade in after slight delay
        FontButton play_button = findViewById(R.id.playbutton);
        AnimatorSet fade_in_1 = (AnimatorSet) AnimatorInflater.loadAnimator(this, R.animator.menubuttons_fadein);
        fade_in_1.setStartDelay(200);
        fade_in_1.setTarget(play_button);
        fade_in_1.start();
    }

    /*
    Handle user pressing the "Play" button
     */
    public void onPlayPressed(View view) {
        // TODO: play a "button clicked" sound
        // Launch the GameActivity
        Intent game_intent = new Intent(this, GameActivity.class);
        startActivity(game_intent);
    }
}
