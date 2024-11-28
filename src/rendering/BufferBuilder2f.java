package src.rendering;

import src.game.Constants;
import src.utility.Logging;
import src.utility.Vec2;
import src.utility.Vec3;

import java.awt.*;

/**
 * Some abstracted functions for building a buffer.
 */
public class BufferBuilder2f {
    private static final int DEFAULT_SIZE = Constants.BUFF_SIZE_SMALL;
    private static final int POS_FLOAT_COUNT = 2;

    private float[] vertices;
    private int size;
    private boolean autoResize;

    private int floatCount = 0;
    private int vertexCount = 0;
    private int separationsCount = 0;

    private int additionalVertFloats = 0;
    private int floatCountPerVert = POS_FLOAT_COUNT;

    public BufferBuilder2f() {this(DEFAULT_SIZE, false, 0);}
    public BufferBuilder2f(int size) {this(size, false, 0);}
    public BufferBuilder2f(boolean autoResize) {this(DEFAULT_SIZE, autoResize, 0);}
    public BufferBuilder2f(boolean autoResize, int additionalVertFloats) {this(DEFAULT_SIZE, autoResize, additionalVertFloats);}
    public BufferBuilder2f(int size, boolean autoResize, int additionalVertFloats){
        vertices = new float[size];
        this.size = size;
        this.autoResize = autoResize;
        setAdditionalVertFloats(additionalVertFloats);
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

    public void setAdditionalVertFloats(int num) {
        if (additionalVertFloats != 0 && additionalVertFloats != num)
            Logging.warn("Changing already set 'additional vertices'. This could warp the buffer");
        additionalVertFloats = num;
        floatCountPerVert = POS_FLOAT_COUNT + num;
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

        float[] f = new float[floatCountPerVert * 2];  // pushing 2 vertices
        f[0] = vertices[floatCount-2- additionalVertFloats];  // just trust me here bro
        f[1] = vertices[floatCount-1- additionalVertFloats];
        f[2+ additionalVertFloats] = toX;
        f[2+ additionalVertFloats +1] = toY;
        pushRawVertices(f);
    }

    public void pushRawSeparatedVertices(float[] verts) {
        pushSeparation(verts[0], verts[1]);
        pushRawVertices(verts);
    }

    public void pushRawVertices(float[] verts) {
        int fCount = verts.length;
        if (fCount == 0) return;

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
        vertexCount += fCount / floatCountPerVert;
    }

    public void pushSeparatedQuad(Shape.Quad q) {
        pushSeparation(q.a);
        pushQuad(q);
    }

    public void pushQuad(Shape.Quad q) {
        Vec3 va = q.mode.getVar(0); Vec3 vb = q.mode.getVar(1);
        Vec3 vc = q.mode.getVar(2); Vec3 vd = q.mode.getVar(3);
        pushRawVertices(new float[] {
                q.a.x, q.a.y, q.mode.type, va.x, va.y, va.z,
                q.b.x, q.b.y, q.mode.type, vb.x, vb.y, vb.z,
                q.c.x, q.c.y, q.mode.type, vc.x, vc.y, vc.z,
                q.d.x, q.d.y, q.mode.type, vd.x, vd.y, vd.z
        });
    }

    public void pushSeparatedPolygon(Shape.Poly p) {
        pushSeparation(p.points.getFirst().add(p.pos));
        pushPolygon(p);
    }

    /** first, second, third, ... */
    public void pushPolygon(Shape.Poly p) {
        float[] verts = new float[p.points.size() * floatCountPerVert];
        int i = 0;
        for (Vec2 point : p.points) {
            Vec3 mv = p.mode.getVar(i);
            int inx = i * floatCountPerVert;
            verts[inx] = point.x + p.pos.x; verts[inx+1] = point.y + p.pos.y;
            verts[inx+2] = p.mode.type;
            verts[inx+3] = mv.x; verts[inx+4] = mv.y; verts[inx+5] = mv.z;
            i++;
        }
        pushRawVertices(verts);
    }

    public void pushSeparatedPolygonSorted(Shape.Poly p) {
        pushSeparation(p.points.getFirst().add(p.pos));
        pushPolygonSorted(p);
    }

    /** first, last, first+1, last-1, ... */
    public void pushPolygonSorted(Shape.Poly p) {
        float[] verts = new float[p.points.size() * floatCountPerVert];
        for (int i = 0; i < p.points.size(); i++) {
            int offset = (int) (i / 2f);  // floor

            Vec2 point;
            if (i % 2 == 0) point = p.points.get(offset);  // front
            else point = p.points.get(p.points.size()-1 - offset);  // back

            Vec3 mv = p.mode.getVar(i);
            int inx = i * floatCountPerVert;
            verts[inx] = point.x + p.pos.x; verts[inx+1] = point.y + p.pos.y;
            verts[inx+2] = p.mode.type;
            verts[inx+3] = mv.x; verts[inx+4] = mv.y; verts[inx+5] = mv.z;
        }
        pushRawVertices(verts);
    }

    /** Circles should be rendered as instanced GL_TRIANGLES */
    public void pushCircle(Vec2 pos, float radius, Color col) {
        pushCircle(pos, radius, 0, col);
    }

    /** Circles should be rendered as instanced GL_TRIANGLES */
    public void pushCircle(Vec2 pos, float radius, float outline, Color col) {
        pushRawVertices(new float[] {
                pos.x, pos.y, radius, outline, col.getRed(), col.getGreen(), col.getBlue()
        });
    }

    public void appendBuffer(BufferBuilder2f builder2f) {appendBuffer(builder2f, false);}
    public void appendBuffer(BufferBuilder2f builder2f, boolean useSeparation) {
        if (builder2f.getVertexCount() == 0) return;

        if (useSeparation) pushRawSeparatedVertices(builder2f.getSetVertices());
        else pushRawVertices(builder2f.getSetVertices());
    }

    public void prependBuffer(BufferBuilder2f builder2f) {prependBuffer(builder2f, false);}
    public void prependBuffer(BufferBuilder2f builder2f, boolean useSeparation) {
        if (builder2f.getVertexCount() == 0) return;

        float[] temp = getSetVertices();
        clear();
        pushRawVertices(builder2f.getSetVertices());
        if (useSeparation) pushRawSeparatedVertices(temp);
        else pushRawVertices(temp);
    }
}
