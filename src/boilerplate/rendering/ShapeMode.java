package boilerplate.rendering;

import boilerplate.common.BoilerplateConstants;
import boilerplate.utility.Vec2;
import boilerplate.utility.Vec3;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public abstract class ShapeMode<T> {
    public abstract T getVar(int inx);

    public static class CustomInx<T> extends ShapeMode<T> {
        List<T> vars;

        @Override
        public T getVar(int inx) {
            return null;
        }
    }

    public static class Type extends ShapeMode<Vec3> {
        List<Vec3> vars;
        int type;

        public Type() {this(BoilerplateConstants.MODE_NIL);}
        public Type(int texSlot, Vec2 texTopLeft, Vec2 texSize) {
            this(BoilerplateConstants.MODE_TEX);
            this.vars = List.of(new Vec3[] {
                    new Vec3(texTopLeft, texSlot),
                    new Vec3(texTopLeft.add(0, texSize.y), texSlot),
                    new Vec3(texTopLeft.add(texSize.x, 0), texSlot),
                    new Vec3(texTopLeft.add(texSize), texSlot)
            });
        }
        public Type(Color col) {
            this(BoilerplateConstants.MODE_COL);
            this.vars = List.of(new Vec3[] {new Vec3(col)});
        }

        public Type(int mode) {this.type = mode;}
        public Type(int mode, Vec3... modeVars) {
            this(mode);
            this.vars = Arrays.stream(modeVars).toList();
        }

        /** Get vec3 at inx, or last (or empty vec3 if no vars exist) */
        public Vec3 getVar(int inx) {
            if (vars == null || vars.isEmpty()) return new Vec3();
            if (inx >= vars.size()) return vars.getLast();
            return vars.get(inx);
        }
    }
}
