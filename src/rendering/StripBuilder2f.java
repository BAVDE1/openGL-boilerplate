package src.rendering;

import src.game.Constants;
import src.utility.Logging;
import src.utility.Vec2f;

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
    private void addSeparation(float toX, float toY) {
        assert floatCount > 1;
        separationsCount++;

        float[] f = new float[4 + (additionalVerts * 2)];
        f[0] = vertices[floatCount -2-additionalVerts];  // just trust me here bro
        f[1] = vertices[floatCount -1-additionalVerts];
        f[2+additionalVerts] = toX;
        f[2+additionalVerts+1] = toY;
        pushVertices(f);
    }

    public void pushSeparatedVertices(float[] verts) {
        pushSeparatedVertices(verts, verts.length);
    }

    public void pushSeparatedVertices(float[] verts, int vertsFloatCount) {
        if (floatCount > 0 && vertsFloatCount > 2) addSeparation(verts[0], verts[1]);
        pushVertices(verts, vertsFloatCount);
    }

    public void pushVertices(float[] verts) {
        pushVertices(verts, verts.length);
    }

    public void pushVertices(float[] verts, int vertsFloatCount) {
        assert verts.length > 0;
        if (floatCount + vertsFloatCount > size) {
            Logging.danger("Cannot add an additional '%s' items to an array at '%s' fullness, with '%s / %s' items already set. Aborting.",
                    vertsFloatCount, getCurrentFullnessPercent(), floatCount, size);
            Logging.expensive("Consider allowing more space at the initialization of this StripBuilder!");
            return;
        }

        for (float v : verts) {
            vertices[floatCount] = v;
            floatCount++;
        }

        // todo: allow for non-default layouts
        vertexCount += vertsFloatCount / VertexArray.Layout.defaultLayoutTotalFloatCount();
    }

    public void pushSeparatedTri(Vec2f a, Vec2f b, Vec2f c) {
        if (floatCount > 0) addSeparation(a.x, a.y);
        pushTri(a, b, c);
    }

    public void pushTri(Vec2f a, Vec2f b, Vec2f c) {
        pushVertices(new float[] {
                a.x, a.y, 2, 0, 0, 0,
                b.x, b.y, 2, 0, 0, 0,
                c.x, c.y, 2, 0, 0, 0
        }, 3);
    }

    public void pushSeparatedRect(Vec2f topLeft, Vec2f size) {

    }

    public void pushRect(Vec2f topLeft, Vec2f size) {

    }

    public void pushQuad(Vec2f a, Vec2f b, Vec2f c, Vec2f d) {

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
}
