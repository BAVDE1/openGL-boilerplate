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
        List<Vec3> vars;

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
        // rect layout:
        // 1 3
        // 2 4
        public Quad(Vec2 topLeft, Vec2 size) {
            this.a = topLeft;
            this.b = topLeft.add(0, size.y);
            this.c = topLeft.add(size.x, 0);
            this.d = topLeft.add(size);
        }
        public Quad(Vec2 topLeft, Vec2 size, Mode mode) {
            this(topLeft, size);
            this.mode = mode;
        }
    }
}
