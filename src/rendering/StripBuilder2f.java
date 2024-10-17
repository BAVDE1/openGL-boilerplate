package src.rendering;

import src.game.Constants;
import src.utility.Logging;
import src.utility.Vec2;
import src.utility.Vec3;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class StripBuilder2f {
    private static final int DEFAULT_SIZE = Constants.BUFF_SIZE_SMALL;
    private float[] vertices;
    private int size;
    private boolean autoResize;

    private int floatCount = 0;
    private int vertexCount = 0;
    private int separationsCount = 0;

    private int additionalVerts = 0;

    public StripBuilder2f() {this(DEFAULT_SIZE, false);}
    public StripBuilder2f(int size) {this(size, false);}
    public StripBuilder2f(boolean autoResize) {this(DEFAULT_SIZE, true);}
    public StripBuilder2f(int size, boolean autoResize){
        vertices = new float[size];
        this.size = size;
        this.autoResize = autoResize;
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
        pushRawVertices(f);
    }

    public void clear() {
        vertices = new float[size];
        floatCount = 0;
        vertexCount = 0;
        separationsCount = 0;
    }

    /** Resize buffer and copy already set elements across (if there are any) */
    public void resizeBufferAndKeepElements(int newSize) {
        // store temporarily
        float[] temp = getSetVertices();
        resizeBufferAndWipe(newSize);

        // place back verts
        if (temp.length > newSize) System.arraycopy(temp, 0, temp, 0, newSize-1);
        pushRawVertices(temp);
    }

    public void resizeBufferAndWipe(int newSize) {
        size = newSize;
        clear();
    }

    public void setAdditionalVerts(int num) {
        if (additionalVerts != 0 && additionalVerts != num)
            Logging.warn("Changing already set 'additional vertices'. This could warp the buffer");
        additionalVerts = num;
    }

    public float[] getSetVertices() {
        float[] v = new float[floatCount];
        System.arraycopy(vertices, 0, v, 0, floatCount);
        return v;
    }

    public float getCurrentFullnessPercent() {
        return (float) getSetVertices().length / size;
    }

    public int getBufferSize() {return size;}
    public int getFloatCount() {return floatCount;}
    public int getVertexCount() {return vertexCount;}
    public int getSeparationsCount() {return separationsCount;}
    public boolean isAutoResizing() {return autoResize;}
    public void setAutoResize(boolean val) {autoResize = val;}

    private int autoResizeBuffer(int minSize) {
        int newSize = Constants.findNextLargestBuffSize(minSize);
        if (newSize == Constants.ERROR) return Constants.ERROR;
        resizeBufferAndKeepElements(newSize);
        return size;
    }

    /** Get vec3 at inx, or last (or empty vec3 if no vars exist) */
    private static Vec3 getVar(List<Vec3> vars, int inx) {
        if (vars.isEmpty()) return new Vec3();
        if (inx >= vars.size()) return vars.getLast();
        return vars.get(inx);
    }

    /**
     * PUSHING VERTICES & SHAPES
     */

    public void pushRawSeparatedVertices(float[] verts) {
        addSeparation(verts[0], verts[1]);
        pushRawVertices(verts);
    }

    public void pushRawVertices(float[] verts) {
        int fCount = verts.length;
        assert fCount > 0;
        if (floatCount + fCount > size) {
            if (!autoResize) {
                Logging.danger("Cannot add an additional '%s' items to an array at '%s' fullness, with '%s / %s' items already set. Aborting.",
                        fCount, getCurrentFullnessPercent(), floatCount, size);
                Logging.expensive("Consider setting autoResize to true! (or manually allow more space at the initialization of this StripBuilder)");
                return;
            }

            if (autoResizeBuffer(floatCount + fCount) == Constants.ERROR) {
                Logging.danger("An error occurred attempting to resize this buffer! Aborting.");
                return;
            }
        }

        for (float v : verts) {
            vertices[floatCount] = v;
            floatCount++;
        }
        vertexCount += fCount / (2 + additionalVerts);
    }

    public void pushSeparatedRect(Vec2 topLeft, Vec2 size) {
        addSeparation(topLeft);
        pushRect(topLeft, size);
    }

    public void pushSeparatedRect(Vec2 topLeft, Vec2 size, int mode) {
        addSeparation(topLeft);
        pushRect(topLeft, size, mode);
    }

    public void pushSeparatedRect(Vec2 topLeft, Vec2 size, int texSlot, Vec2 texTopLeft, Vec2 texSize) {
        addSeparation(topLeft);
        pushRect(topLeft, size, texSlot, texTopLeft, texSize);
    }

    public void pushSeparatedRect(Vec2 topLeft, Vec2 size, Color col) {
        addSeparation(topLeft);
        pushRect(topLeft, size, col);
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
        // 1 3
        // 2 4
        pushRawVertices(new float[] {
                topLeft.x, topLeft.y, mode, getVar(v, 0).x, getVar(v, 0).y, getVar(v, 0).z,
                topLeft.x, btmR.y,    mode, getVar(v, 1).x, getVar(v, 1).y, getVar(v, 1).z,
                btmR.x, topLeft.y,    mode, getVar(v, 2).x, getVar(v, 2).y, getVar(v, 2).z,
                btmR.x, btmR.y,       mode, getVar(v, 3).x, getVar(v, 3).y, getVar(v, 3).z
        });
    }
}
