package boilerplate.rendering.builders;

import boilerplate.common.BoilerplateConstants;
import boilerplate.utility.Logging;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;

/**
 * Some abstracted functions for building a buffer.
 */
public class BufferBuilder {
    protected static final int DEFAULT_SIZE = BoilerplateConstants.BUFF_SIZE_DEFAULT;

    protected float[] floats;
    protected int size;
    protected boolean autoResize;

    private int posFloatCount = 0;
    private int floatCount = 0;
    private int vertexCount = 0;
    private int separationsCount = 0;

    private int additionalVertFloats = 0;
    private int floatCountPerVert = getPosFloatCount();

    private boolean shouldNextBeSeparated = false;  // should the next time raw vertices are pushed be separated

    public BufferBuilder() {this(DEFAULT_SIZE, false, 0);}
    public BufferBuilder(int size) {this(size, false, 0);}
    public BufferBuilder(boolean autoResize) {this(DEFAULT_SIZE, autoResize, 0);}
    public BufferBuilder(boolean autoResize, int additionalVertFloats) {this(DEFAULT_SIZE, autoResize, additionalVertFloats);}
    public BufferBuilder(int size, boolean autoResize, int additionalVertFloats){
        floats = new float[size];
        this.size = size;
        this.autoResize = autoResize;
        setAdditionalVertFloats(additionalVertFloats);
    }

    public int getPosFloatCount() {
        return posFloatCount;
    }

    public void setPosFloatCount(int count) throws IllegalStateException {
        posFloatCount = count;
    }

    public void clear() {
        floats = new float[size];
        floatCount = 0;
        vertexCount = 0;
        separationsCount = 0;
    }

    /** Resize buffer and copy already set elements across (if there are any) */
    public void resizeBufferAndKeepElements(int newSize) {
        // store temporarily
        float[] temp = getFloats();
        resizeBufferAndWipe(newSize);

        // place back verts
        if (temp.length > newSize) System.arraycopy(temp, 0, temp, 0, newSize-1);
        pushRawFloats(temp);
    }

    public void resizeBufferAndWipe(int newSize) {
        size = newSize;
        clear();
    }

    public void setAdditionalVertFloats(int num) {
        if (additionalVertFloats != 0 && additionalVertFloats != num)
            Logging.warn("Changing already set 'additional vertices'. This could warp the buffer");
        additionalVertFloats = num;
        floatCountPerVert = getPosFloatCount() + num;
    }

    public float[] getFloats() {
        float[] v = new float[floatCount];
        System.arraycopy(floats, 0, v, 0, floatCount);
        return v;
    }

    /** Returns a slice of the current floats in the buffer */
    public float[] getFloatsSlice(int startInx, int length) {
        float[] slice = new float[length];
        System.arraycopy(floats, startInx, slice, 0, length);
        return slice;
    }

    /** Returns the last N vertices in the buffer */
    public float[] getLastVertices(int vertexCount) {
        int length = vertexCount * floatCountPerVert;
        return getFloatsSlice(floatCount - length, length);
    }

    public float getCurrentFullnessPercent() {
        return (float) getFloats().length / size;
    }

    public int getBufferSize() {return size;}
    public int getFloatCount() {return floatCount;}
    public int getVertexCount() {return vertexCount;}
    public int getSeparationsCount() {return separationsCount;}
    public boolean isAutoResizing() {return autoResize;}
    public void setAutoResize(boolean val) {autoResize = val;}

    private int autoResizeBuffer(int minSize) {
        int newSize = BoilerplateConstants.findNextLargestBuffSize(minSize);
        if (newSize == BoilerplateConstants.ERROR) return BoilerplateConstants.ERROR;
        resizeBufferAndKeepElements(newSize);
        return size;
    }

    /**
     * PUSHING VERTICES & SHAPES
     */

    public void setFloatsUnsafe(float[] floats, int destInx) {
        System.arraycopy(floats, 0, this.floats, destInx, floats.length);
    }

    public void pushRawSeparatedFloats(float[] newFloats) {
        pushRawFloats(newFloats, true);
    }

    public void pushRawFloats(float[] newFloats) {
        pushRawFloats(newFloats, false);
    }

    public void pushRawFloats(float[] newFloats, boolean separation) {
        int fCount = newFloats.length;
        if (fCount == 0) return;

        // do before resize
        if (separation || shouldNextBeSeparated) performSeparation(newFloats);
        if (floatCount + fCount > size) {
            if (attemptResize(fCount) == BoilerplateConstants.ERROR) return;
        }

        System.arraycopy(newFloats, 0, floats, floatCount, fCount);
        floatCount += fCount;
        vertexCount += fCount / floatCountPerVert;
    }

    /** pushing 2 vertices (last of current verts and first of new verts) */
    private void performSeparation(float[] verts) {
        shouldNextBeSeparated = false;
        if (floatCount >= floatCountPerVert) {
            separationsCount++;
            float[] separationVerts = new float[floatCountPerVert * 2];
            System.arraycopy(floats, floatCount - floatCountPerVert, separationVerts, 0, floatCountPerVert);
            System.arraycopy(verts, 0, separationVerts, floatCountPerVert, floatCountPerVert);
            pushRawFloats(separationVerts, false);  // FALSE!!!
        }
    }

    private int attemptResize(int newFloatCount) {
        if (!autoResize) {
            Logging.danger("Cannot add an additional '%s' items to an array at '%s' fullness, with '%s / %s' items already set. Aborting.",
                    newFloatCount, getCurrentFullnessPercent(), floatCount, size);
            Logging.purple("Consider setting autoResize to true! (or manually allow more space at the initialization of this builder)");
            return BoilerplateConstants.ERROR;
        }

        // attempt resize
        if (autoResizeBuffer(floatCount + newFloatCount) == BoilerplateConstants.ERROR) {
            Logging.danger("An error occurred attempting to resize this buffer! Aborting.");
            return BoilerplateConstants.ERROR;
        }
        return 1;
    }

    private int unpackIntoArray(float[] theArray, int destInx, int vertInx, ShapeMode.Unpack unpack) {
        int unpackInx = vertInx % unpack.unpackVars.size();
        float[] unpackVars = unpack.unpackVars.get(unpackInx);
        System.arraycopy(unpackVars, 0, theArray, destInx, unpackVars.length);
        return unpackVars.length;
    }

    private int appendToArray(float[] theArray, int destInx, ShapeMode.Append append) {
        System.arraycopy(append.vars, 0, theArray, destInx, append.vars.length);
        return append.vars.length;
    }

    private void addPointsToArray(float[] theArray, int floatInx, int vertInx, Vector2f point, ShapeMode mode) {
        theArray[floatInx] = point.x;
        theArray[floatInx+1] = point.y;

        if (mode instanceof ShapeMode.Demonstration demo) {
            Vector3f typeVar = demo.getVar(vertInx);
            theArray[floatInx + 2] = demo.type;
            theArray[floatInx + 3] = typeVar.x;
            theArray[floatInx + 4] = typeVar.y;
            theArray[floatInx + 5] = typeVar.z;
        } else if (mode instanceof ShapeMode.Unpack unpack) {
            unpackIntoArray(theArray, floatInx+2, vertInx, unpack);
        } else if (mode instanceof ShapeMode.Append append) {
            appendToArray(theArray, floatInx+2, append);
        } else if (mode instanceof ShapeMode.UnpackAppend unpackAppend) {
            int numUnpacked = unpackIntoArray(theArray, floatInx+2, vertInx, unpackAppend.unpack);
            appendToArray(theArray, floatInx+2+numUnpacked, unpackAppend.append);
        } else if (mode instanceof ShapeMode.AppendUnpack appendUnpack) {
            int numAppended = appendToArray(theArray, floatInx+2, appendUnpack.append);
            unpackIntoArray(theArray, floatInx+2+numAppended, vertInx, appendUnpack.unpack);
        }
    }

    public void pushSeparatedPolygon(Shape2d.Poly p) {
        shouldNextBeSeparated = true;
        pushPolygon(p);
    }

    /** first, second, third, ... */
    public void pushPolygon(Shape2d.Poly p) {
        float[] verts = new float[p.points.size() * floatCountPerVert];
        for (int i = 0; i < p.points.size(); i++) {
            int inx = i * floatCountPerVert;
            Vector2f point = p.points.get(i);
            addPointsToArray(verts, inx, i, point.add(p.pos, new Vector2f()), p.mode);
        }
        pushRawFloats(verts);
    }

    public void pushSeparatedPolygonSorted(Shape2d.Poly p) {
        shouldNextBeSeparated = true;
        pushPolygonSorted(p);
    }

    /** first, last, first+1, last-1, ... */
    public void pushPolygonSorted(Shape2d.Poly p) {
        float[] verts = new float[p.points.size() * floatCountPerVert];
        for (int i = 0; i < p.points.size(); i++) {
            int inx = i * floatCountPerVert;
            int offset = (int) (i / 2f);  // floor

            // choose which point to add
            Vector2f point;
            if (i % 2 == 0) point = p.points.get(offset);  // front
            else point = p.points.get(p.points.size()-1 - offset);  // back
            addPointsToArray(verts, inx, i, point.add(p.pos, new Vector2f()), p.mode);
        }
        pushRawFloats(verts);
    }

    /** Circles should be rendered as instanced GL_TRIANGLES */
    public void pushCircle(Vector2f pos, float radius, Color col) {
        pushCircle(pos, radius, 0, col);
    }

    /** Circles should be rendered as instanced GL_TRIANGLES */
    public void pushCircle(Vector2f pos, float radius, float outline, Color col) {
        pushRawFloats(new float[] {
                pos.x, pos.y, radius, outline, col.getRed(), col.getGreen(), col.getBlue()
        });
    }

    public void appendBuffer(BufferBuilder builder) {appendBuffer(builder, false);}
    public void appendBuffer(BufferBuilder builder, boolean useSeparation) {
        if (builder.getVertexCount() == 0) return;

        if (useSeparation) pushRawSeparatedFloats(builder.getFloats());
        else pushRawFloats(builder.getFloats());
    }

    public void prependBuffer(BufferBuilder builder) {prependBuffer(builder, false);}
    public void prependBuffer(BufferBuilder builder, boolean useSeparation) {
        if (builder.getVertexCount() == 0) return;

        float[] temp = getFloats();
        clear();
        pushRawFloats(builder.getFloats());
        if (useSeparation) pushRawSeparatedFloats(temp);
        else pushRawFloats(temp);
    }
}
