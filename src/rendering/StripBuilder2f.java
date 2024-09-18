package src.rendering;

import src.utility.Logging;

public class StripBuilder2f {
    private float[] vertices;
    private final int size;
    private int count = 0;
    private int separations = 0;

    public StripBuilder2f(){this(1024);}
    public StripBuilder2f(int size){
        vertices = new float[size];
        this.size = size;
    }

    /** Adds 2 "invisible" vertices */
    private void addSeparation(float toX, float toY) {
        assert count > 1;
        separations++;
        pushVertices(new float[] {
                vertices[count-2], vertices[count-1],
                toX, toY
        });
    }

    public void pushSeparatedVertices(float[] verts) {
        pushSeparatedVertices(verts, verts.length);
    }

    public void pushSeparatedVertices(float[] verts, int additionCount) {
        assert verts.length > 1;
        if (count > 0) addSeparation(verts[0], verts[1]);
        pushVertices(verts, additionCount);
    }

    public void pushVertices(float[] verts) {
        pushVertices(verts, verts.length);
    }

    public void pushVertices(float[] verts, int additionCount) {
        assert verts.length > 0;
        if (count + additionCount > size) {
            Logging.danger(String.format(
                    "Attempting to add too many items to primitive array! Array max size: %s, current array count %s, attempted addition: %s. Aborting",
                    size, count, additionCount));
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

    public float[] getVertices() {
        return vertices;
    }
}
