package src.utility;

import src.game.Constants;

import java.awt.*;

public class Vec2f {
    public float x;
    public float y;

    public Vec2f(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public Vec2f(float d) {
        this(d, d);
    }

    public Vec2f(Vec2f vec2) {
        this(vec2.x, vec2.y);
    }

    public Vec2f(Dimension dim) {
        this(dim.width, dim.height);
    }

    public Vec2f() {
        this(0, 0);
    }

    public Vec2f(Point point) {
        if (point == null) {
            point = new Point(0, 0);
        }
        this(point.x, point.y);
    }

    public void set(Vec2f vec) {
        set(vec.x, vec.y);
    }

    public void set(float x, float y) {
        this.x = x;
        this.y = y;
    }
    
    public Vec2f getClone() {
        return new Vec2f(x, y);
    }

    public static Vec2f fromDim(Dimension dim) {
        return new Vec2f(dim);
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

    public Vec2f normaliseSelf() {
        return normalise(this);
    }

    public static Vec2f normalise(Vec2f out) {
        float lsq = (float) out.lengthSq();
        if (lsq > Constants.EPSILON_SQ) {
            float inv_len = (float) (1 / Math.sqrt(out.lengthSq()));
            out.mulSelf(inv_len);
        }
        return out;
    }

    public float dot(Vec2f vec) {
        return x * vec.x + y * vec.y;
    }

    public void clampSelf(float min, float max) {
        clampSelf(new Vec2f(min, min), new Vec2f(max, max));
    }

    public void clampSelf(Vec2f min, Vec2f max) {
        x = Math.max(min.x, Math.min(max.x, x));
        y = Math.max(min.y, Math.min(max.y, y));
    }

    public void negateSelf() {
        set(negate());
    }

    public Vec2f negate() {
        return new Vec2f(-x, -y);
    }

    public Vec2f cross(float v) {
        return new Vec2f(y * v, x * -v);
    }

    public float cross(Vec2f vec) {
        return x * vec.y - y * vec.x;
    }

    public Vec2f cross() {
        return new Vec2f(y, -x);
    }

    public boolean equals(Vec2f other) {
        return x == other.x && y == other.y;
    }

    /**
     * =========
     * BASIC USE
     * =========
     */

    public void addSelf(float d) {
        x += d;
        y += d;
    }

    public void addSelf(Vec2f vec) {
        x += vec.x;
        y += vec.y;
    }

    public Vec2f add(float d) {
        return add(this, d);
    }

    public Vec2f add(Vec2f vec) {
        return add(this, vec);
    }

    public static Vec2f add(Vec2f vec, float d) {
        return new Vec2f(vec.x + d, vec.y + d);
    }

    public static Vec2f add(Vec2f vec1, Vec2f vec2) {
        return new Vec2f(vec1.x + vec2.x, vec1.y + vec2.y);
    }

    public void subSelf(float d) {
        x -= d;
        y -= d;
    }

    public void subSelf(Vec2f vec) {
        x -= vec.x;
        y -= vec.y;
    }

    public Vec2f sub(float d) {
        return sub(this, d);
    }

    public Vec2f sub(Vec2f vec) {
        return sub(this, vec);
    }

    public static Vec2f sub(Vec2f vec, float d) {
        return new Vec2f(vec.x - d, vec.y - d);
    }

    public static Vec2f sub(Vec2f vec1, Vec2f vec2) {
        return new Vec2f(vec1.x - vec2.x, vec1.y - vec2.y);
    }

    public void divSelf(float d) {
        x /= d;
        y /= d;
    }

    public void divSelf(Vec2f vec) {
        x /= vec.x;
        y /= vec.y;
    }

    public Vec2f div(float d) {
        return div(this, d);
    }

    public Vec2f div(Vec2f vec) {
        return div(this, vec);
    }

    public static Vec2f div(Vec2f vec, float d) {
        return new Vec2f(vec.x / d, vec.y / d);
    }

    public static Vec2f div(Vec2f vec1, Vec2f vec2) {
        return new Vec2f(vec1.x / vec2.x, vec1.y / vec2.y);
    }

    public void mulSelf(float d) {
        x *= d;
        y *= d;
    }

    public void mulSelf(Vec2f vec) {
        x *= vec.x;
        y *= vec.y;
    }

    public Vec2f mul(float d) {
        return mul(this, d);
    }

    public Vec2f mul(Vec2f vec) {
        return mul(this, vec);
    }

    public static Vec2f mul(Vec2f vec, float d) {
        return new Vec2f(vec.x * d, vec.y * d);
    }

    public static Vec2f mul(Vec2f vec1, Vec2f vec2) {
        return new Vec2f(vec1.x * vec2.x, vec1.y * vec2.y);
    }

    @Override
    public String toString() {
        return String.format("Vec2(x=%.2f, y=%.2f)", x, y);
    }
}
