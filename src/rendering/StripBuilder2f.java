package src.rendering;

import src.game.Constants;
import src.utility.Logging;
import src.utility.Vec2;
import src.utility.Vec3;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class StripBuilder2f {
    private float[] vertices;
    private int size;

    public int floatCount = 0;
    public int vertexCount = 0;
    public int separationsCount = 0;

    private int additionalVerts = 0;

    public StripBuilder2f(){this(Constants.BUFF_SIZE_SMALL);}
    public StripBuilder2f(int size){
        vertices = new float[size];
        this.size = size;
    }

    /** Adds 2 "invisible" vertices */
    private void addSeparation(Vec2 v) {addSeparation(v.x, v.y);}
    private void addSeparation(float toX, float toY) {
        if (floatCount < 2) return;
        separationsCount++;

        float[] f = new float[4 + (additionalVerts * 2)];
        f[0] = vertices[floatCount-2-additionalVerts];  // just trust me here bro
        f[1] = vertices[floatCount-1-additionalVerts];
        f[2+additionalVerts] = toX;
        f[2+additionalVerts+1] = toY;
        pushVertices(f);
    }

    public void clear() {
        vertices = new float[size];
        floatCount = 0;
        vertexCount = 0;
        separationsCount = 0;
    }

    public float[] getSetVertices() {
        float[] v = new float[floatCount];
        System.arraycopy(vertices, 0, v, 0, floatCount);
        return v;
    }

    public void setAdditionalVerts(int num) {
        if (additionalVerts != 0 && additionalVerts != num)
            Logging.warn("Changing already set 'additional vertices'. This could warp the buffer");
        additionalVerts = num;
    }

    public float getCurrentFullnessPercent() {
        return (float) getSetVertices().length / size;
    }

    /** Resize buffer and copy already set elements across (if there are any) */
    public void resizeBufferAndKeepElements(int newSize) {
        // store temporarily
        float[] temp = getSetVertices();
        resizeBufferAndWipe(newSize);

        // place back verts
        pushVertices(temp);
    }

    public void resizeBufferAndWipe(int newSize) {
        size = newSize;
        clear();
    }

    public int getBufferSize() {
        return size;
    }

    /** Get vec3 at inx, or last, or empty */
    private static Vec3 getVar(List<Vec3> vars, int inx) {
        if (vars.isEmpty()) return new Vec3();
        if (inx >= vars.size()) return vars.getLast();
        return vars.get(inx);
    }

    /**
     * PUSHING VERTICES & SHAPES
     */

    public void pushSeparatedVertices(float[] verts) {
        addSeparation(verts[0], verts[1]);
        pushVertices(verts);
    }

    public void pushVertices(float[] verts) {
        int fCount = verts.length;
        assert fCount > 0;
        if (floatCount + fCount > size) {
            Logging.danger("Cannot add an additional '%s' items to an array at '%s' fullness, with '%s / %s' items already set. Aborting.",
                    fCount, getCurrentFullnessPercent(), floatCount, size);
            Logging.expensive("Consider allowing more space at the initialization of this StripBuilder!");
            return;
        }

        for (float v : verts) {
            vertices[floatCount] = v;
            floatCount++;
        }
        vertexCount += fCount / (2 + additionalVerts);
    }

    public void pushSeparatedRect(Vec2 topLeft, Vec2 size) {
        addSeparation(topLeft); pushRect(topLeft, size);
    }

    public void pushSeparatedRect(Vec2 topLeft, Vec2 size, int texSlot, Vec2 texTopLeft, Vec2 texSize) {
        addSeparation(topLeft); pushRect(topLeft, size, texSlot, texTopLeft, texSize);
    }

    public void pushSeparatedRect(Vec2 topLeft, Vec2 size, Color col) {
        addSeparation(topLeft); pushRect(topLeft, size, col);
    }

    public void pushRect(Vec2 topLeft, Vec2 size) {
        pushRect(topLeft, size, Constants.MODE_NIL);
    }

    public void pushRect(Vec2 topLeft, Vec2 size, int texSlot, Vec2 texTopLeft, Vec2 texSize) {
        pushRect(topLeft, size, Constants.MODE_TEX,
                new Vec3(texTopLeft, texSlot),
                new Vec3(texTopLeft.add(0, texSize.y), texSlot),
                new Vec3(texTopLeft.add(texSize.x, 0), texSlot),
                new Vec3(texTopLeft.add(texSize), texSlot)
        );
    }

    public void pushRect(Vec2 topLeft, Vec2 size, Color col) {
        pushRect(topLeft, size, Constants.MODE_COL, new Vec3(col));
    }

    public void pushRect(Vec2 topLeft, Vec2 size, int mode, Vec3... modeVars) {
        Vec2 btmR = topLeft.add(size);
        List<Vec3> v = Arrays.stream(modeVars).toList();
        // 2 4
        // 1 3
        pushVertices(new float[] {
                topLeft.x, topLeft.y, mode, getVar(v, 0).x, getVar(v, 0).y, getVar(v, 0).z,
                topLeft.x, btmR.y,    mode, getVar(v, 1).x, getVar(v, 1).y, getVar(v, 1).z,
                btmR.x, topLeft.y,    mode, getVar(v, 2).x, getVar(v, 2).y, getVar(v, 2).z,
                btmR.x, btmR.y,       mode, getVar(v, 3).x, getVar(v, 3).y, getVar(v, 3).z
        });
    }
}
