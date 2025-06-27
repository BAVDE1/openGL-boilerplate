package boilerplate.models;

import boilerplate.utility.Logging;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Animator {
    private final HashMap<String, Animation> animations = new HashMap<>();
    private String currentAnimation;
    private float animationTime;

    public Model.NodeData rootNode;
    public Matrix4f[] finalBoneMatrices;

    public void init(int boneCount) {
        finalBoneMatrices = new Matrix4f[boneCount];
        for (int i = 0; i < boneCount; i++) {
            finalBoneMatrices[i] = new Matrix4f().identity();
        }
    }

    public void addAnimation(Animation animation) {
        animations.put(animation.name, animation);
    }

    public void removeAnimation(String animationName) {
        animations.remove(animationName);
    }

    public void playAnimation(String animationName) {
        animationTime = 0;
        currentAnimation = animationName;
    }

    public void stopPlayingAnimation() {
        animationTime = 0;
        currentAnimation = null;
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
        animationTime += (animation.ticksPerSecond * .2f) * dt;
        animationTime = animationTime % animation.duration;
        calcBoneTransformations(rootNode, new Matrix4f().identity());
    }

    private void calcBoneTransformations(Model.NodeData node, Matrix4f parentTransform) {
        Animation currentAnim = getCurrentAnimation();
        Matrix4f nodeTransform = node.transform;
        AnimatedBone bone = currentAnim.getAnimatedBone(node.name);

        if (bone != null) nodeTransform = bone.calcInterpolatedMatrix(animationTime);
        Matrix4f globalTransform = parentTransform.mul(nodeTransform, new Matrix4f());

        if (currentAnim.boneInfoMap.containsKey(node.name)) {
            Model.BoneInfo boneInfo = currentAnim.boneInfoMap.get(node.name);
            finalBoneMatrices[boneInfo.id] = globalTransform.mul(boneInfo.offset, new Matrix4f());
//            finalBoneMatrices[boneInfo.id] = new Matrix4f().identity();
        }

        for (Model.NodeData child : node.children) {
            calcBoneTransformations(child, globalTransform);
        }
    }
}
