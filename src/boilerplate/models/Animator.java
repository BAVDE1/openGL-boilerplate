package boilerplate.models;

import boilerplate.utility.Logging;
import org.joml.Matrix4f;

import java.util.HashMap;

public class Animator {
    private final Model model;
    private final HashMap<String, Animation> animations = new HashMap<>();
    private String currentAnimation;
    private float animationTime;

    public Model.NodeData rootNode;
    public Matrix4f[] finalBoneMatrices;
    Matrix4f globalInvTrans;

    public Animator(Model model) {
        this.model = model;
    }

    public void init(int boneCount, Model.NodeData rootNode) {
        this.rootNode = rootNode;
        this.globalInvTrans = rootNode.transform.invert(new Matrix4f());

        this.finalBoneMatrices = new Matrix4f[boneCount];
        resetBoneMatrices(rootNode, new Matrix4f().identity());
    }

    public void addAnimation(Animation animation) {
        animations.put(animation.name, animation);
    }

    public void removeAnimation(String animationName) {
        animations.remove(animationName);
    }

    public void playAnimation(String animationName) {
        if (!animations.containsKey(animationName)) {
            Logging.danger("No animation '%s' exists for this model, %s", animationName, model);
            return;
        }
        animationTime = 0;
        currentAnimation = animationName;
    }

    public void stopPlayingAnimation(boolean resetBoneMatrices) {
        animationTime = 0;
        currentAnimation = null;

        if (resetBoneMatrices) resetBoneMatrices(rootNode, new Matrix4f().identity());
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
        animationTime += (animation.ticksPerSecond * 1) * dt;
        animationTime = animationTime % animation.duration;
        calcBoneTransformations(rootNode, rootNode.transform);
    }

    private void calcBoneTransformations(Model.NodeData node, Matrix4f parentTransform) {
        Animation currentAnim = getCurrentAnimation();
        AnimatedBone animatedBone = currentAnim.getAnimatedBone(node.name);
        Matrix4f globalTransform;

        if (animatedBone != null) {
            Bone bone = animatedBone.bone;
            Matrix4f boneTransform = animatedBone.calcInterpolatedMatrix(animationTime);
            globalTransform = parentTransform.mul(boneTransform, new Matrix4f());
            finalBoneMatrices[bone.id] = new Matrix4f(model.rootNodeInvTrans).mul(globalTransform.mul(bone.offset, new Matrix4f()));
        } else {
            globalTransform = parentTransform.mul(node.transform, new Matrix4f());
        }

        for (Model.NodeData child : node.children) {
            calcBoneTransformations(child, globalTransform);
        }
    }

    public void resetBoneMatrices(Model.NodeData node, Matrix4f parentTransform) {
        Matrix4f globalTransform = parentTransform.mul(node.transform, new Matrix4f());

        Bone bone = model.getBone(node.name);
        if (bone != null) {
            finalBoneMatrices[bone.id] = globalTransform.mul(bone.offset, new Matrix4f());
        }

        for (Model.NodeData child : node.children) {
            resetBoneMatrices(child, globalTransform);
        }
    }
}
