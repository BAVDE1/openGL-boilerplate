package boilerplate.rendering.builders;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

public class BufferBuilder3f extends BufferBuilder {
    public BufferBuilder3f() {this(DEFAULT_SIZE, false, 0);}
    public BufferBuilder3f(int size) {this(size, false, 0);}
    public BufferBuilder3f(boolean autoResize) {this(DEFAULT_SIZE, autoResize, 0);}
    public BufferBuilder3f(boolean autoResize, int additionalVertFloats) {this(DEFAULT_SIZE, autoResize, additionalVertFloats);}
    public BufferBuilder3f(int size, boolean autoResize, int additionalVertFloats){
        floats = new float[size];
        this.size = size;
        this.autoResize = autoResize;
        setAdditionalVertFloats(additionalVertFloats);
    }

    @Override
    public int getPosFloatCount() {
        return 3;
    }

    @Override
    public void setPosFloatCount(int count) throws IllegalStateException {
        throw new IllegalStateException("Cannot override the position float count for this BufferBuilder");
    }

    public void pushSeparatedPolygon(Shape3d.Poly3d p) {
        shouldNextBeSeparated = true;
        pushPolygon(p);
    }

    /** first, second, third, ... */
    public void pushPolygon(Shape3d.Poly3d p) {
        float[] verts = new float[p.points.size() * floatCountPerVert];
        for (int i = 0; i < p.points.size(); i++) {
            int inx = i * floatCountPerVert;
            Vector3f point = p.points.get(i);
            FloatBuffer pointPos = BufferUtils.createFloatBuffer(3);
            point.add(p.pos, new Vector3f()).get(pointPos);
            addPointsToArray(verts, inx, i, pointPos, p.mode);
        }
        pushRawFloats(verts);
    }
}
