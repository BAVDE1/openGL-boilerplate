package boilerplate.models;

import boilerplate.utility.MathUtils;
import org.joml.Matrix4f;
import org.lwjgl.assimp.AIBone;

public class Bone {
    int id;
    String name;
    Matrix4f offset;

    public Bone(int boneId, AIBone aiBone) {
        this.id = boneId;
        this.name = aiBone.mName().dataString();
        this.offset = MathUtils.AIMatrixToMatrix(aiBone.mOffsetMatrix());
    }
}
