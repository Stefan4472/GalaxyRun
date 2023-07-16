package com.galaxyrun.engine;

import com.galaxyrun.engine.draw.DrawInstruction;
import com.galaxyrun.sprite.Sprite;
import com.galaxyrun.util.ProtectedQueue;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Stefan on 9/1/2020.
 */

public class DrawLayers {
    private final GameContext gameContext;
    private int numLayers;
    private List<Sprite>[] layers;  // TODO: NAMING IS HORRIBLE


    // TODO: HAVE A WAY TO ASSIGN A DRAW LAYER TO A SPRITE TYPE
    public DrawLayers(GameContext gameContext, int numLayers) {
        this.gameContext = gameContext;
        this.numLayers = numLayers;
        layers = new LinkedList[numLayers];
        // TODO: `FASTLIST` STRUCTURE?
        for (int i = 0; i < numLayers; i++){
            layers[i] = new LinkedList<>();
        }
    }

    public void addSprite(Sprite sprite) {
        layers[sprite.getDrawLayer()].add(sprite);
    }

    public void getDrawInstructions(ProtectedQueue<DrawInstruction> drawQueue)
    {
        for (List<Sprite> sprite_layer : layers) {
            for (Sprite sprite : sprite_layer) {
                sprite.getDrawInstructions(drawQueue);
                if (gameContext.inDebugMode) {
                    drawQueue.push(sprite.drawHitbox());
                }
            }
        }
    }

    public void clear() {
        for (List<Sprite> sprite_layer : layers) {
            sprite_layer.clear();
        }
    }
}