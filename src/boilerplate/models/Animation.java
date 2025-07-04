package boilerplate.models;

import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.AIAnimation;
import org.lwjgl.assimp.AINodeAnim;

import java.util.HashMap;

public class Animation {
    private static int nameCounter = 0;

    private final Model model;
    String name;
    float duration;
    float ticksPerSecond;

//    List<AnimatedBone> animatedBones = new ArrayList<>();
    HashMap<String, AnimatedBone> animatedBones = new HashMap<>();

    public Animation(AIAnimation aiAnimation, Model model) {
        this.model = model;
        processAnimation(aiAnimation);
    }

    private void processAnimation(AIAnimation aiAnimation) {
        name = aiAnimation.mName().dataString();
        if (name.isEmpty()) name = "anim_%s".formatted(nameCounter++);
        duration = (float) aiAnimation.mDuration();
        ticksPerSecond = (float) aiAnimation.mTicksPerSecond();

        PointerBuffer allChannels = aiAnimation.mChannels();
        if (allChannels == null) return;

        while (allChannels.hasRemaining()) {
            try (AINodeAnim aiNodeAnim = AINodeAnim.create(allChannels.get())) {
                String relatedBoneName = aiNodeAnim.mNodeName().dataString();
                Bone bone = model.getBone(relatedBoneName);
                if (bone == null) continue;
                AnimatedBone animatedBone = new AnimatedBone(aiNodeAnim, bone);
                animatedBones.put(animatedBone.bone.name, animatedBone);
            }
        }
    }

    public AnimatedBone getAnimatedBone(String boneName) {
        if (!animatedBones.containsKey(boneName)) return null;
        return animatedBones.get(boneName);
    }
}
