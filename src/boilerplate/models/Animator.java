package boilerplate.models;

import org.lwjgl.assimp.AIAnimation;

import java.util.HashMap;

public class Animator {
    private final HashMap<String, Animation> animations = new HashMap<>();

    public void addAnimation(AIAnimation aiAnimation) {
        String name = aiAnimation.mName().dataString();
        Animation anim = new Animation(name);

        anim.duration = aiAnimation.mDuration();
        anim.ticksPerSecond = aiAnimation.mTicksPerSecond();

        animations.put(name, anim);
    }

    public void removeAnimation(String name) {
        animations.remove(name);
    }
}
