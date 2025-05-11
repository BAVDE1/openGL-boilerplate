package boilerplate.rendering.builders;

import boilerplate.common.BoilerplateConstants;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public abstract class ShapeMode {
    /**
     * Appends the given vars to end of each vertex
     */
    public static class Append extends ShapeMode {
        public float[] vars;

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
        List<Vector3f> vars;
        int type;

        public Demonstration() {this(BoilerplateConstants.DEMO_MODE_NIL);}
        public Demonstration(int texSlot, Vector2f texTopLeft, Vector2f texSize) {
            this(BoilerplateConstants.DEMO_MODE_TEX);
            this.vars = List.of(new Vector3f[] {
                    new Vector3f(texTopLeft, texSlot),
                    new Vector3f(texTopLeft.add(0, texSize.y), texSlot),
                    new Vector3f(texTopLeft.add(texSize.x, 0), texSlot),
                    new Vector3f(texTopLeft.add(texSize), texSlot)
            });
        }
        public Demonstration(Color col) {
            this(BoilerplateConstants.DEMO_MODE_COL);
            this.vars = List.of(new Vector3f[] {new Vector3f(col.getRed(), col.getGreen(), col.getBlue())});
        }

        public Demonstration(int mode) {this.type = mode;}
        public Demonstration(int mode, Vector3f... modeVars) {
            this(mode);
            this.vars = Arrays.stream(modeVars).toList();
        }

        /** Get Vector3f at inx, or last (or empty Vector3f if no vars exist) */
        public Vector3f getVar(int inx) {
            if (vars == null || vars.isEmpty()) return new Vector3f();
            if (inx >= vars.size()) return vars.getLast();
            return vars.get(inx);
        }
    }
}
