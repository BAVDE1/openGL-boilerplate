package boilerplate.models;

import org.joml.Matrix4f;
import org.lwjgl.assimp.AIBone;

import java.util.HashMap;

public class Bone {
    int id;
    String name;
    Matrix4f offset;  // transforms vertex from model space to (this) bone space
//    HashMap<Integer, Float> vertexWeights = new HashMap<>();  // key: vertexId, value: weight

    public Bone(String name) {
        this.name = name;
    }
}
