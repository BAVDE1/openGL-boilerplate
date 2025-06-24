package boilerplate.models;

import org.joml.Matrix4f;

public class Bone {
    int id;
    String name;
    Matrix4f offset;  // transforms vertex from model space to (this) bone space

    public Bone(String name) {
        this.name = name;
    }
}
