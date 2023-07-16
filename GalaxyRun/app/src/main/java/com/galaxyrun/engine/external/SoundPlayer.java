package com.galaxyrun.engine.external;


import android.content.Context;
import android.media.AudioAttributes;
import android.media.SoundPool;

import com.galaxyrun.engine.audio.SoundID;

import java.util.HashMap;

// TODO: does this belong in GameEngine?
public class SoundPlayer {
    private final Context appContext;
    private final SoundPool soundPool;
    // Map SoundID to "resID" loaded by the SoundPool
    private final HashMap<SoundID, Integer> resIds;

    public SoundPlayer(Context appContext) {
        this.appContext = appContext;
        SoundPool.Builder builder = new SoundPool.Builder();
        builder.setMaxStreams(1);
        builder.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
        soundPool = builder.build();
        resIds = new HashMap<>();
        // Load all sounds on init so that there is no delay when playing for the first time
        loadAllSounds();
    }

    private void loadAllSounds() {
        for (SoundID soundID : SoundID.values()) {
            loadSound(soundID);
        }
    }

    // TODO: Pretty sure this will never return null, but *could* it?
    private int loadSound(SoundID sound) {
        int resId = soundPool.load(appContext, sound.getId(), 1);
        resIds.put(sound, resId);
        return resId;
    }

    public void playSound(SoundID sound) {
        Integer resId = resIds.get(sound);
        if (resId == null) {
            resId = loadSound(sound);
        }
        // TODO: guard against null
        soundPool.play(resId, 1.0f, 1.0f, 1, 0, 1.0f);
    }

    // Release all associated memory
    public void release() {
        soundPool.release();
    }
}