package boilerplate.models;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AINodeAnim;

import java.util.HashMap;

public class Animation {
    String name;
    float duration;
    float ticksPerSecond;

    HashMap<String, Model.BoneInfo> boneInfoMap;

//    List<AnimatedBone> animatedBones = new ArrayList<>();
    HashMap<String, AnimatedBone> animatedBones = new HashMap<>();

    public Animation(AIAnimation aiAnimation, HashMap<String, Model.BoneInfo> boneInfoMap) {
        this.boneInfoMap = boneInfoMap;
        processAnimation(aiAnimation);
    }

    private void processAnimation(AIAnimation aiAnimation) {
        name = aiAnimation.mName().dataString();
        duration = (float) aiAnimation.mDuration();
        ticksPerSecond = (float) aiAnimation.mTicksPerSecond();

        PointerBuffer allChannels = aiAnimation.mChannels();
        if (allChannels == null) return;

        while (allChannels.hasRemaining()) {
            try (AINodeAnim aiNodeAnim = AINodeAnim.create(allChannels.get())) {
                AnimatedBone bone = new AnimatedBone(aiNodeAnim);
                bone.id = boneInfoMap.get(bone.name).id;
                animatedBones.put(bone.name, bone);
            }
        }
    }

    public AnimatedBone getAnimatedBone(String boneName) {
        if (!animatedBones.containsKey(boneName)) return null;
        return animatedBones.get(boneName);
    }
}
