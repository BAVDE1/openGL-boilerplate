package src.rendering;

import src.game.Constants;
import src.utility.Logging;

import java.util.Arrays;

public class StripBuilder2f {
    private float[] vertices;
    private final int size;
    public int count = 0;
    public int separations = 0;

    private int additionalVerts = 0;

    public StripBuilder2f(){this(Constants.BUFF_SIZE_DEFAULT);}
    public StripBuilder2f(int size){
        vertices = new float[size];
        this.size = size;
    }

    /** Adds 2 "invisible" vertices */
    private void addSeparation(float toX, float toY) {
        assert count > 1;
        separations++;

        float[] f = new float[4 + (additionalVerts * 2)];
        f[0] = vertices[count-2-additionalVerts];  // just trust me here bro
        f[1] = vertices[count-1-additionalVerts];
        f[2+additionalVerts] = toX;
        f[2+additionalVerts+1] = toY;
        pushVertices(f);
    }

    public void pushSeparatedVertices(float[] verts) {
        pushSeparatedVertices(verts, verts.length);
    }

    public void pushSeparatedVertices(float[] verts, int vertCount) {
        assert verts.length > 1;
        if (count > 0) addSeparation(verts[0], verts[1]);
        pushVertices(verts, vertCount);
    }

    public void pushVertices(float[] verts) {
        pushVertices(verts, verts.length);
    }

    public void pushVertices(float[] verts, int vertCount) {
        assert verts.length > 0;
        if (count + vertCount > size) {
            Logging.danger("Attempting to add too many items to primitive array! Array max size: %s, current array count %s, attempted addition: %s. Aborting",
                    size, count, vertCount);
            return;
        }

        for (float v : verts) {
            vertices[count] = v;
            count++;
        }
    }

    public void clear() {
        vertices = new float[size];
        count = 0;
        separations = 0;
    }

    public float[] getAllVertices() {
        return vertices;
    }

    public float[] getSetVertices() {
        float[] v = new float[count];
        System.arraycopy(vertices, 0, v, 0, count);
        return v;
    }

    public void setAdditionalVerts(int num) {
        if (additionalVerts != 0 && additionalVerts != num)
            Logging.warn("Changing already set 'additional vertices'. This could warp the buffer");
        additionalVerts = num;
    }
}
