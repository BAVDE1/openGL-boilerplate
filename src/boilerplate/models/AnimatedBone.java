package boilerplate.models;

import boilerplate.utility.Logging;
import boilerplate.utility.MathUtils;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.lwjgl.assimp.AINodeAnim;
import org.lwjgl.assimp.AIQuatKey;
import org.lwjgl.assimp.AIVectorKey;

import java.util.ArrayList;
import java.util.List;

public class AnimatedBone {
    public static class KeyPosition {
        Vector3f position;
        float timestamp;
    }

    public static class KeyRotation {
        Quaternionf orientation;
        float timestamp;
    }

    public static class KeyScale {
        Vector3f scale;
        float timestamp;
    }

    final Bone bone;

    List<KeyPosition> keyPositions = new ArrayList<>();
    List<KeyRotation> keyRotations = new ArrayList<>();
    List<KeyScale> keyScales = new ArrayList<>();

    public AnimatedBone(AINodeAnim aiNodeAnim, Bone bone) {
        this.bone = bone;

        processPosKeys(aiNodeAnim.mPositionKeys());
        processRotKeys(aiNodeAnim.mRotationKeys());
        processScaleKeys(aiNodeAnim.mScalingKeys());
    }

    private void processPosKeys(AIVectorKey.Buffer positionKeys) {
        if (positionKeys == null) return;
        while (positionKeys.hasRemaining()) {
            AIVectorKey key = positionKeys.get();
            KeyPosition newKey = new KeyPosition();
            newKey.position = MathUtils.AIVectorToVector(key.mValue());
            newKey.timestamp = (float) key.mTime();
            keyPositions.add(newKey);
        }
    }

    private void processRotKeys(AIQuatKey.Buffer rotationKeys) {
        if (rotationKeys == null) return;
        while (rotationKeys.hasRemaining()) {
            AIQuatKey key = rotationKeys.get();
            KeyRotation newKey = new KeyRotation();
            newKey.orientation = MathUtils.AIQuatToQuat(key.mValue());
            newKey.timestamp = (float) key.mTime();
            keyRotations.add(newKey);
        }
    }

    private void processScaleKeys(AIVectorKey.Buffer scaleKeys) {
        if (scaleKeys == null) return;
        while (scaleKeys.hasRemaining()) {
            AIVectorKey key = scaleKeys.get();
            KeyScale newKey = new KeyScale();
            newKey.scale = MathUtils.AIVectorToVector(key.mValue());
            newKey.timestamp = (float) key.mTime();
            keyScales.add(newKey);
        }
    }

    public Matrix4f calcInterpolatedMatrix(float animationTime) {
        Matrix4f out = new Matrix4f().identity();
        interpolatePosition(animationTime, out);
        interpolateRotation(animationTime, out);
        interpolateScale(animationTime, out);
        return out;
    }

    private int getPositionKeyInx(float animationTime) {
        for (int i = 0; i < keyPositions.size() - 1; i++) {
            if (animationTime < keyPositions.get(i + 1).timestamp) return i;
        }
        return 0;
    }

    private int getRotationKeyInx(float animationTime) {
        for (int i = 0; i < keyRotations.size() - 1; i++) {
            if (animationTime < keyRotations.get(i + 1).timestamp) return i;
        }
        return 0;
    }

    private int getScaleKeyInx(float animationTime) {
        for (int i = 0; i < keyScales.size() - 1; i++) {
            if (animationTime < keyScales.get(i + 1).timestamp) return i;
        }
        return 0;
    }

    private float getTimestampPercentCompletion(float lastTimeStamp, float nextTimeStamp, float animationTime) {
        float current = animationTime - lastTimeStamp;
        float framesDiff = nextTimeStamp - lastTimeStamp;
        return current / framesDiff;
    }

    private void interpolatePosition(float animationTime, Matrix4f out) {
        if (keyPositions.size() == 1) {
            out.translate(keyPositions.getFirst().position);
            return;
        }

        int currentKeyInx = getPositionKeyInx(animationTime);
        int nextKeyInx = currentKeyInx + 1;
        float t1 = keyPositions.get(currentKeyInx).timestamp;
        float t2 = keyPositions.get(nextKeyInx).timestamp;
        float percent = getTimestampPercentCompletion(t1, t2, animationTime);
        Vector3f position = keyPositions.get(currentKeyInx).position.lerp(keyPositions.get(nextKeyInx).position, percent, new Vector3f());
        out.translate(position);
    }

    private void interpolateRotation(float animationTime, Matrix4f out) {
        if (keyRotations.size() == 1) {
            out.rotate(keyRotations.getFirst().orientation.normalize(new Quaternionf()));
            return;
        }

        int currentKeyInx = getRotationKeyInx(animationTime);
        int nextKeyInx = currentKeyInx + 1;
        float percent = getTimestampPercentCompletion(keyRotations.get(currentKeyInx).timestamp, keyRotations.get(nextKeyInx).timestamp, animationTime);
        Quaternionf rotation = keyRotations.get(currentKeyInx).orientation.slerp(keyRotations.get(nextKeyInx).orientation, percent, new Quaternionf());
        out.rotate(rotation.normalize());
    }

    private void interpolateScale(float animationTime, Matrix4f out) {
        if (keyScales.size() == 1) {
            out.scale(keyScales.getFirst().scale);
            return;
        }

        int currentKeyInx = getScaleKeyInx(animationTime);
        int nextKeyInx = currentKeyInx + 1;
        float percent = getTimestampPercentCompletion(keyScales.get(currentKeyInx).timestamp, keyScales.get(nextKeyInx).timestamp, animationTime);
        Vector3f scale = keyScales.get(currentKeyInx).scale.lerp(keyScales.get(nextKeyInx).scale, percent, new Vector3f());
        out.scale(scale);
    }

    @Override
    public String toString() {
        return "AnimatedBone(%s, %s)".formatted(bone.id, bone.name);
    }
}
