package src.utility;

import src.game.Constants;

import java.awt.*;

public class Vec2 {
    public double x;
    public double y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2(double d) {
        this(d, d);
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
        this(point.x, point.y);
    }

    public void set(Vec2 vec) {
        set(vec.x, vec.y);
    }

    public void set(double x, double y) {
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

    public Vec2 normaliseSelf() {
        return normalise(this);
    }

    public static Vec2 normalise(Vec2 out) {
        double lsq = out.lengthSq();
        if (lsq > Constants.EPSILON_SQ) {
            double inv_len = 1 / Math.sqrt(out.lengthSq());
            out.mulSelf(inv_len);
        }
        return out;
    }

    public double dot(Vec2 vec) {
        return x * vec.x + y * vec.y;
    }

    public void clampSelf(double min, double max) {
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

    public Vec2 cross(double v) {
        return new Vec2(y * v, x * -v);
    }

    public double cross(Vec2 vec) {
        return x * vec.y - y * vec.x;
    }

    public Vec2 cross() {
        return new Vec2(y, -x);
    }

    public boolean equals(Vec2 other) {
        return x == other.x && y == other.y;
    }

    /**
     * =========
     * BASIC USE
     * =========
     */

    public void addSelf(double d) {
        x += d;
        y += d;
    }

    public void addSelf(Vec2 vec) {
        x += vec.x;
        y += vec.y;
    }

    public Vec2 add(double d) {
        return add(this, d);
    }

    public Vec2 add(Vec2 vec) {
        return add(this, vec);
    }

    public static Vec2 add(Vec2 vec, double d) {
        return new Vec2(vec.x + d, vec.y + d);
    }

    public static Vec2 add(Vec2 vec1, Vec2 vec2) {
        return new Vec2(vec1.x + vec2.x, vec1.y + vec2.y);
    }

    public void subSelf(double d) {
        x -= d;
        y -= d;
    }

    public void subSelf(Vec2 vec) {
        x -= vec.x;
        y -= vec.y;
    }

    public Vec2 sub(double d) {
        return sub(this, d);
    }

    public Vec2 sub(Vec2 vec) {
        return sub(this, vec);
    }

    public static Vec2 sub(Vec2 vec, double d) {
        return new Vec2(vec.x - d, vec.y - d);
    }

    public static Vec2 sub(Vec2 vec1, Vec2 vec2) {
        return new Vec2(vec1.x - vec2.x, vec1.y - vec2.y);
    }

    public void divSelf(double d) {
        x /= d;
        y /= d;
    }

    public void divSelf(Vec2 vec) {
        x /= vec.x;
        y /= vec.y;
    }

    public Vec2 div(double d) {
        return div(this, d);
    }

    public Vec2 div(Vec2 vec) {
        return div(this, vec);
    }

    public static Vec2 div(Vec2 vec, double d) {
        return new Vec2(vec.x / d, vec.y / d);
    }

    public static Vec2 div(Vec2 vec1, Vec2 vec2) {
        return new Vec2(vec1.x / vec2.x, vec1.y / vec2.y);
    }

    public void mulSelf(double d) {
        x *= d;
        y *= d;
    }

    public void mulSelf(Vec2 vec) {
        x *= vec.x;
        y *= vec.y;
    }

    public Vec2 mul(double d) {
        return mul(this, d);
    }

    public Vec2 mul(Vec2 vec) {
        return mul(this, vec);
    }

    public static Vec2 mul(Vec2 vec, double d) {
        return new Vec2(vec.x * d, vec.y * d);
    }

    public static Vec2 mul(Vec2 vec1, Vec2 vec2) {
        return new Vec2(vec1.x * vec2.x, vec1.y * vec2.y);
    }

    @Override
    public String toString() {
        return String.format("Vec2(x=%.2f, y=%.2f)", x, y);
    }
}
