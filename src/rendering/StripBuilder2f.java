package src.rendering;

import src.game.Constants;
import src.utility.Logging;
import src.utility.Vec2;
import src.utility.Vec3;

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

    /**
     * PUSHING VERTICES & SHAPES
     */

    // Adds 2 "invisible" vertices
    private void pushSeparation(Vec2 v) {pushSeparation(v.x, v.y);}
    private void pushSeparation(float toX, float toY) {
        if (floatCount < 2) return;
        separationsCount++;

        float[] f = new float[(2 + additionalVerts) * 2];  // pushing 2 vertices
        f[0] = vertices[floatCount-2-additionalVerts];  // just trust me here bro
        f[1] = vertices[floatCount-1-additionalVerts];
        f[2+additionalVerts] = toX;
        f[2+additionalVerts+1] = toY;
        pushRawVertices(f);
    }

    public void pushRawSeparatedVertices(float[] verts) {
        pushSeparation(verts[0], verts[1]);
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

        System.arraycopy(verts, 0, vertices, floatCount, fCount);
        floatCount += fCount;
        vertexCount += fCount / (2 + additionalVerts);
    }

    public void pushSeparatedQuad(Shape.Quad q) {
        pushSeparation(q.a);
        pushQuad(q);
    }

    public void pushQuad(Shape.Quad q) {
        Vec3 v1 = q.mode.getVar(0); Vec3 v2 = q.mode.getVar(1);
        Vec3 v3 = q.mode.getVar(2); Vec3 v4 = q.mode.getVar(3);
        pushRawVertices(new float[] {
                q.a.x, q.a.y, q.mode.type, v1.x, v1.y, v1.z,
                q.b.x, q.b.y, q.mode.type, v2.x, v2.y, v2.z,
                q.c.x, q.c.y, q.mode.type, v3.x, v3.y, v3.z,
                q.d.x, q.d.y, q.mode.type, v4.x, v4.y, v4.z
        });
    }

    public void pushSeparatedPolygon(Shape.Poly p) {
        pushSeparation(p.points.getFirst());
        pushPolygon(p);
    }

    public void pushPolygon(Shape.Poly p) {
        // loop each point to build float[]
    }
}
