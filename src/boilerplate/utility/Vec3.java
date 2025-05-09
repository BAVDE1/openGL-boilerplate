package boilerplate.utility;

import boilerplate.common.BoilerplateConstants;

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

    public Vec3() {
        this(0);
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

    public void set(float f) {
        set(f, f, f);
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

    public float roll() {
        return z;
    }

    public float pitch() {
        return y;
    }

    public float yaw() {
        return x;
    }

    /**
     * =======
     * ADV USE
     * =======
     */

    public double lengthSq() {
        return (x * x + y * y + z * z);
    }

    public double length() {
        return Math.sqrt(lengthSq());
    }

    public void normaliseSelf() {
        normalise(this);
    }

    public Vec3 normalized() {
        return normalise(getClone());
    }

    public static Vec3 normalise(Vec3 out) {
        float lsq = (float) out.lengthSq();
        if (lsq > BoilerplateConstants.EPSILON_SQ) {
            float inv_len = (float) (1 / Math.sqrt(out.lengthSq()));
            out.mulSelf(inv_len);
        }
        return out;
    }

    public float dot(Vec3 vec) {
        return x * vec.x + y * vec.y + z * vec.z;
    }

    public void clampSelf(float min, float max) {
        clampSelf(new Vec3(min), new Vec3(max));
    }

    public void clampSelf(Vec3 min, Vec3 max) {
        x = Math.max(min.x, Math.min(max.x, x));
        y = Math.max(min.y, Math.min(max.y, y));
        z = Math.max(min.z, Math.min(max.z, z));
    }

    public void negateSelf() {
        set(negate());
    }

    public Vec3 negate() {
        return new Vec3(-x, -y, -z);
    }

    public Vec3 cross(Vec3 vec) {
        return new Vec3(
                (y * vec.z) - (z * vec.y),
                (z * vec.x) - (x * vec.z),
                (x * vec.y) - (y * vec.x)
        );
    }

    public boolean equals(Vec3 other) {
        return x == other.x && y == other.y && z == other.z;
    }

    public boolean equals(float f) {
        return x == f && y == f && z == f;
    }

    /**
     * =========
     * BASIC USE
     * =========
     */

    public void addSelf(float f) {
        x += f;
        y += f;
        z += f;
    }

    public void addSelf(Vec3 vec) {
        x += vec.x;
        y += vec.y;
        z += vec.z;
    }

    public Vec3 add(float f) {
        return add(this, f);
    }

    public Vec3 add(float x, float y, float z) {
        return new Vec3(this.x + x, this.y + y, this.z + z);
    }

    public Vec3 add(Vec3 vec) {
        return add(this, vec);
    }

    public static Vec3 add(Vec3 vec, float f) {
        return new Vec3(vec.x + f, vec.y + f, vec.z + f);
    }

    public static Vec3 add(Vec3 vecA, Vec3 vecB) {
        return new Vec3(vecA.x + vecB.x, vecA.y + vecB.y, vecA.z + vecB.z);
    }

    public void subSelf(float f) {
        x -= f;
        y -= f;
        z -= f;
    }

    public void subSelf(Vec3 vec) {
        x -= vec.x;
        y -= vec.y;
        z -= vec.z;
    }

    public Vec3 sub(float f) {
        return sub(this, f);
    }

    public Vec3 sub(float x, float y, float z) {
        return new Vec3(this.x - x, this.y - y, this.z - z);
    }

    public Vec3 sub(Vec3 vec) {
        return sub(this, vec);
    }

    public static Vec3 sub(Vec3 vec, float f) {
        return new Vec3(vec.x - f, vec.y - f, vec.z - f);
    }

    public static Vec3 sub(Vec3 vecA, Vec3 vecB) {
        return new Vec3(vecA.x - vecB.x, vecA.y - vecB.y, vecA.z - vecB.z);
    }

    public void divSelf(float f) {
        x /= f;
        y /= f;
        z /= f;
    }

    public void divSelf(Vec3 vec) {
        x /= vec.x;
        y /= vec.y;
        z /= vec.z;
    }

    public Vec3 div(float f) {
        return div(this, f);
    }

    public Vec3 div(Vec3 vec) {
        return div(this, vec);
    }

    public static Vec3 div(Vec3 vec, float f) {
        return new Vec3(vec.x / f, vec.y / f, vec.z / f);
    }

    public static Vec3 div(Vec3 vecA, Vec3 vecB) {
        return new Vec3(vecA.x / vecB.x, vecA.y / vecB.y, vecA.z / vecB.z);
    }

    public void mulSelf(float f) {
        x *= f;
        y *= f;
    }

    public void mulSelf(Vec3 vec) {
        x *= vec.x;
        y *= vec.y;
    }

    public Vec3 mul(float f) {
        return mul(this, f);
    }

    public Vec3 mul(Vec3 vec) {
        return mul(this, vec);
    }

    public static Vec3 mul(Vec3 vec, float f) {
        return new Vec3(vec.x * f, vec.y * f, vec.z * f);
    }

    public static Vec3 mul(Vec3 vecA, Vec3 vecB) {
        return new Vec3(vecA.x * vecB.x, vecA.y * vecB.y, vecA.z + vecB.z);
    }

    @Override
    public String toString() {
        return String.format("Vec3(%s, %s, %s)", x, y, z);
    }
}
