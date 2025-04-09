package boilerplate.rendering;

import boilerplate.common.BoilerplateConstants;
import boilerplate.utility.Vec2;
import boilerplate.utility.Vec3;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public abstract class ShapeMode {
    /**
     * Appends the given vars to end of each vertex
     */
    public static class Append extends ShapeMode {
        float[] vars;

        public Append(float[] varsToAppend) {
            vars = varsToAppend;
        }
    }

    /**
     * Unpacks the given vars to the end of each vertex (wraps)
     */
    public static class Unpack extends ShapeMode {
        List<float[]> unpackVars;

        public Unpack(List<float[]> unpackVars) {
            this.unpackVars = unpackVars;
        }
    }

    /**
     * Unpacks the unpackVars for each vertex in order (wraps when it reaches the end)
     * And then appends appendVars for each vertex
     */
    public static class UnpackAppend extends ShapeMode {
        Unpack unpack;
        Append append;

        public UnpackAppend(List<float[]> unpackVars, float[] appendVars) {
            unpack = new Unpack(unpackVars);
            append = new Append(appendVars);
        }
    }

    /**
     * same as UnpackAppend, but reversed
     */
    public static class AppendUnpack extends ShapeMode {
        Unpack unpack;
        Append append;

        public AppendUnpack(float[] appendVars, List<float[]> unpackVars) {
            unpack = new Unpack(unpackVars);
            append = new Append(appendVars);
        }
    }

    /**
     * THIS TYPE IS FOR DEMONSTRATION PURPOSES
     * meant for use with the main shader
     */
    public static class Demonstration extends ShapeMode {
        List<Vec3> vars;
        int type;

        public Demonstration() {this(BoilerplateConstants.MODE_NIL);}
        public Demonstration(int texSlot, Vec2 texTopLeft, Vec2 texSize) {
            this(BoilerplateConstants.MODE_TEX);
            this.vars = List.of(new Vec3[] {
                    new Vec3(texTopLeft, texSlot),
                    new Vec3(texTopLeft.add(0, texSize.y), texSlot),
                    new Vec3(texTopLeft.add(texSize.x, 0), texSlot),
                    new Vec3(texTopLeft.add(texSize), texSlot)
            });
        }
        public Demonstration(Color col) {
            this(BoilerplateConstants.MODE_COL);
            this.vars = List.of(new Vec3[] {new Vec3(col)});
        }

        public Demonstration(int mode) {this.type = mode;}
        public Demonstration(int mode, Vec3... modeVars) {
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
