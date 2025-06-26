package boilerplate.models;

import org.joml.Matrix4f;
import org.lwjgl.assimp.AIAnimation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Animator {
    private final HashMap<String, Animation> animations = new HashMap<>();
    private String currentAnimation;
    private float animationTime;

    public List<Matrix4f> finalBoneMatrices = new ArrayList<>();

    public void addAnimation(Animation animation) {
        animations.put(animation.name, animation);
    }

    public void removeAnimation(String name) {
        animations.remove(name);
    }

    public void playAnimation(String animationName) {
        animationTime = 0;
        currentAnimation = animationName;
    }

    public void stopPlayingAnimation() {
        currentAnimation = null;
        animationTime = 0;
    }

    public boolean hasAnimation(String animationName) {
        return animations.containsKey(animationName);
    }

    public Animation getCurrentAnimation() {
        if (currentAnimation == null) return null;
        return animations.get(currentAnimation);
    }

    public void update(float dt) {
        if (currentAnimation == null) return;

        Animation animation = getCurrentAnimation();
        animationTime += animation.ticksPerSecond * dt;
        animationTime = animationTime % animation.duration;
    }

    private void calcBoneTransformations() {

    }
}
