package src.utility;

import java.awt.*;

/**
 * Setting & getting
 */
public class Vec4 {
    public float x;
    public float y;
    public float z;
    public float w;

    public Vec4(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4(float f) {
        this(f, f, f, f);
    }

    public Vec4(Vec4 vec) {
        this(vec.x, vec.y, vec.z, vec.w);
    }

    public Vec4(Color color) {
        this(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public Vec4(Vec2 vec2, float f) {
        this(vec2.x, vec2.y, f, f);
    }

    public Vec4() {
        this(0, 0, 0, 0);
    }

    public void set(Vec4 vec) {
        set(vec.x, vec.y, vec.z, vec.w);
    }

    public void set(float x, float y, float z, float w) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.w = w;
    }

    public Vec4 getClone() {
        return new Vec4(x, y, z, w);
    }

    public Vec2 xy() {
        return new Vec2(x, y);
    }
    public Vec3 xyz() {
        return new Vec3(x, y, z);
    }
}
