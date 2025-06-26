package boilerplate.models;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIVectorKey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Animation {
    String name;
    float duration;
    float ticksPerSecond;

    List<AnimatedBone> animatedBones = new ArrayList<>();

    public Animation(AIAnimation aiAnimation, HashMap<String, Model.BoneInfo> boneInfoMap) {
        processAnimation(aiAnimation, boneInfoMap);
    }

    private void processAnimation(AIAnimation aiAnimation, HashMap<String, Model.BoneInfo> boneInfoMap) {
        name = aiAnimation.mName().dataString();
        duration = (float) aiAnimation.mDuration();
        ticksPerSecond = (float) aiAnimation.mTicksPerSecond();

        PointerBuffer allChannels = aiAnimation.mChannels();
        if (allChannels == null) return;

        while (allChannels.hasRemaining()) {
            try (AINodeAnim aiNodeAnim = AINodeAnim.create(allChannels.get())) {
                AnimatedBone bone = new AnimatedBone(aiNodeAnim);
                bone.id = boneInfoMap.get(bone.name).id;
                animatedBones.add(bone);
            }
        }
    }
}
