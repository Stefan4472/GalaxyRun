package plainsimple.spaceships;

import android.media.SoundPool;

/**
 * Stores parameters for playing a raw file using SoundPool
 */
public class SoundParams {

    // ID of raw resource to be played
    private RawResource resourceID;
    // left volume value (range = 0.0 to 1.0)
    private float leftVolume;
    // right volume value (range = 0.0 to 1.0)
    private float rightVolume;
    // stream priority (0 = lowest priority)
    private int priority;
    // loop mode (0 = no loop, -1 = loop forever)
    private int loop;
    // playback rate (1.0 = normal playback, range 0.5 to 2.0)
    private float rate;

    public SoundParams(RawResource resourceID, float leftVolume, float rightVolume, int priority, int loop, float rate) {
        this.resourceID = resourceID;
        this.leftVolume = leftVolume;
        this.rightVolume = rightVolume;
        this.priority = priority;
        this.loop = loop;
        this.rate = rate;
    }
}
