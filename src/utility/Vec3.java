package src.utility;

import java.awt.*;

/**
 * Setting & getting
 */
public class Vec3 {
    public float x;
    public float y;
    public float z;

    public Vec3(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3(float f) {
        this(f, f, f);
    }

    public Vec3(Vec3 vec) {
        this(vec.x, vec.y, vec.z);
    }

    public Vec3(Color color) {
        this(color.getRed(), color.getGreen(), color.getBlue());
    }

    public Vec3(Vec2 vec2, float f) {
        this(vec2.x, vec2.y, f);
    }

    public Vec3() {
        this(0, 0, 0);
    }

    public void set(Vec3 vec) {
        set(vec.x, vec.y, vec.z);
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3 getClone() {
        return new Vec3(x, y, z);
    }

    public Vec2 xy() {
        return new Vec2(x, y);
    }
}
