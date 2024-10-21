package src.rendering;

import src.game.Constants;
import src.utility.Vec2;
import src.utility.Vec3;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class Shape {
    public static class Mode {
        int type;
        List<Vec3> vars = List.of(new Vec3[0]);

        public Mode() {this(Constants.MODE_NIL);}
        public Mode(int texSlot, Vec2 texTopLeft, Vec2 texSize) {
            this(Constants.MODE_TEX);
            this.vars = List.of(new Vec3[]{
                    new Vec3(texTopLeft, texSlot),
                    new Vec3(texTopLeft.add(0, texSize.y), texSlot),
                    new Vec3(texTopLeft.add(texSize.x, 0), texSlot),
                    new Vec3(texTopLeft.add(texSize), texSlot)
            });
        }
        public Mode(Color col) {
            this(Constants.MODE_COL);
            this.vars = List.of(new Vec3[] {new Vec3(col)});
        }

        public Mode(int mode) {this.type = mode;}
        public Mode(int mode, Vec3... modeVars) {
            this(mode);
            this.vars = Arrays.stream(modeVars).toList();
        }

        /** Get vec3 at inx, or last (or empty vec3 if no vars exist) */
        public Vec3 getVar(int inx) {
            if (vars.isEmpty()) return new Vec3();
            if (inx >= vars.size()) return vars.getLast();
            return vars.get(inx);
        }
    }

    public static class Quad {
        public Mode mode = new Mode();
        public Vec2 a; public Vec2 b;
        public Vec2 c; public Vec2 d;

        public Quad(Vec2 a, Vec2 b, Vec2 c, Vec2 d) {
            this.a = a; this.b = b;
            this.c = c; this.d = d;
        }
        public Quad(Vec2 a, Vec2 b, Vec2 c, Vec2 d, Mode mode) {
            this(a, b, c, d);
            this.mode = mode;
        }

        @Override
        public String toString() {return String.format("Quad(%s, %s, %s, %s)", a, b, c, d);}
    }

    public static class Polygon {
        public Mode mode = new Mode();
        public List<Vec2> points;

        public Polygon(Vec2... points) {
            this.points = List.of(points);
        }
        public Polygon(Mode mode, Vec2... points) {
            this(points);
            this.mode = mode;
        }
    }

    // 1 3
    // 2 4
    public static Quad createRect(Vec2 topLeft, Vec2 size) {
        return new Quad(
                topLeft,
                topLeft.add(0, size.y),
                topLeft.add(size.x, 0),
                topLeft.add(size)
        );
    }
    public static Quad createRect(Vec2 topLeft, Vec2 size, Mode mode) {
        Quad q = createRect(topLeft, size);
        q.mode = mode;
        return q;
    }

    public static Polygon createRectOutline(Vec2 topLeft, Vec2 size, int thickness) {
        return new Polygon();
    }
    public static Polygon createRectOutline(Vec2 topLeft, Vec2 size, int thickness, Mode mode) {
        Polygon p = createRectOutline(topLeft, size, thickness);
        p.mode = mode;
        return p;
    }

    public static Quad createLine(Vec2 point1, Vec2 point2, int thickness) {
        Vec2 normalised = point2.sub(point1).normaliseSelf();
        Vec2 perp = normalised.perpendicular().mul(thickness * .5f);
        return new Quad(
            point1.add(perp), point1.sub(perp),
            point2.add(perp), point2.sub(perp)
        );
    }
    public static Quad createLine(Vec2 point1, Vec2 point2, int thickness, Mode mode) {
        Quad q = createLine(point1, point2, thickness);
        q.mode = mode;
        return q;
    }
}
