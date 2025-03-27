package utility;

import common.Constants;

import java.awt.*;

/**
 * Setting, getting & functionality
 */
public class Vec2 {
    public float x;
    public float y;

    public Vec2(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2(float f) {
        this(f, f);
    }

    public Vec2(Vec2 vec2) {
        this(vec2.x, vec2.y);
    }

    public Vec2(Dimension dim) {
        this(dim.width, dim.height);
    }

    public Vec2() {
        this(0, 0);
    }

    public Vec2(Point point) {
        if (point == null) {
            point = new Point(0, 0);
        }
        set(point.x, point.y);
    }

    public void set(Vec2 vec) {
        set(vec.x, vec.y);
    }

    public void set(float f) {
        set(f, f);
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public Vec2 getClone() {
        return new Vec2(x, y);
    }

    public static Vec2 fromDim(Dimension dim) {
        return new Vec2(dim);
    }

    public Dimension toDim() {
        return new Dimension((int) x, (int) y);
    }

    /**
     * =======
     * ADV USE
     * =======
     */

    public double lengthSq() {
        return (x * x + y * y);
    }

    public double length() {
        return Math.sqrt(lengthSq());
    }

    public void normaliseSelf() {
        normalise(this);
    }

    public Vec2 normalized() {
        return normalise(getClone());
    }

    public static Vec2 normalise(Vec2 out) {
        float lsq = (float) out.lengthSq();
        if (lsq > Constants.EPSILON_SQ) {
            float inv_len = (float) (1 / Math.sqrt(out.lengthSq()));
            out.mulSelf(inv_len);
        }
        return out;
    }

    public float dot(Vec2 vec) {
        return x * vec.x + y * vec.y;
    }

    public void clampSelf(float min, float max) {
        clampSelf(new Vec2(min, min), new Vec2(max, max));
    }

    public void clampSelf(Vec2 min, Vec2 max) {
        x = Math.max(min.x, Math.min(max.x, x));
        y = Math.max(min.y, Math.min(max.y, y));
    }

    public void negateSelf() {
        set(negate());
    }

    public Vec2 negate() {
        return new Vec2(-x, -y);
    }

    public Vec2 cross(float v) {
        return new Vec2(y * v, x * -v);
    }

    public float cross(Vec2 vec) {
        return x * vec.y - y * vec.x;
    }

    public Vec2 cross() {
        return new Vec2(y, -x);
    }

    public boolean equals(Vec2 other) {
        return x == other.x && y == other.y;
    }

    public boolean equals(float f) {
        return x == f && y == f;
    }

    /**
     * =========
     * BASIC USE
     * =========
     */

    public void addSelf(float f) {
        x += f;
        y += f;
    }

    public void addSelf(Vec2 vec) {
        x += vec.x;
        y += vec.y;
    }

    public Vec2 add(float f) {
        return add(this, f);
    }

    public Vec2 add(float x, float y) {
        return new Vec2(this.x + x, this.y + y);
    }

    public Vec2 add(Vec2 vec) {
        return add(this, vec);
    }

    public static Vec2 add(Vec2 vec, float f) {
        return new Vec2(vec.x + f, vec.y + f);
    }

    public static Vec2 add(Vec2 vec1, Vec2 vec2) {
        return new Vec2(vec1.x + vec2.x, vec1.y + vec2.y);
    }

    public void subSelf(float f) {
        x -= f;
        y -= f;
    }

    public void subSelf(Vec2 vec) {
        x -= vec.x;
        y -= vec.y;
    }

    public Vec2 sub(float f) {
        return sub(this, f);
    }

    public Vec2 sub(float x, float y) {
        return new Vec2(this.x - x, this.y - y);
    }

    public Vec2 sub(Vec2 vec) {
        return sub(this, vec);
    }

    public static Vec2 sub(Vec2 vec, float f) {
        return new Vec2(vec.x - f, vec.y - f);
    }

    public static Vec2 sub(Vec2 vec1, Vec2 vec2) {
        return new Vec2(vec1.x - vec2.x, vec1.y - vec2.y);
    }

    public void divSelf(float f) {
        x /= f;
        y /= f;
    }

    public void divSelf(Vec2 vec) {
        x /= vec.x;
        y /= vec.y;
    }

    public Vec2 div(float f) {
        return div(this, f);
    }

    public Vec2 div(Vec2 vec) {
        return div(this, vec);
    }

    public static Vec2 div(Vec2 vec, float f) {
        return new Vec2(vec.x / f, vec.y / f);
    }

    public static Vec2 div(Vec2 vec1, Vec2 vec2) {
        return new Vec2(vec1.x / vec2.x, vec1.y / vec2.y);
    }

    public void mulSelf(float f) {
        x *= f;
        y *= f;
    }

    public void mulSelf(Vec2 vec) {
        x *= vec.x;
        y *= vec.y;
    }

    public Vec2 mul(float f) {
        return mul(this, f);
    }

    public Vec2 mul(Vec2 vec) {
        return mul(this, vec);
    }

    public static Vec2 mul(Vec2 vec, float f) {
        return new Vec2(vec.x * f, vec.y * f);
    }

    public static Vec2 mul(Vec2 vec1, Vec2 vec2) {
        return new Vec2(vec1.x * vec2.x, vec1.y * vec2.y);
    }

    public Vec2 perpendicular() {
        return perpendicular(this);
    }

    public static Vec2 perpendicular(Vec2 vec) {
        return new Vec2(-vec.y, vec.x);
    }

    @Override
    public String toString() {
        return String.format("Vec2(%.2f, %.2f)", x, y);
    }
}
