package boilerplate.rendering;

import boilerplate.common.BoilerplateConstants;
import boilerplate.utility.Logging;
import boilerplate.utility.Vec2;
import boilerplate.utility.Vec3;

import java.awt.*;

/**
 * Some abstracted functions for building a buffer.
 */
public class BufferBuilder2f {
    private static final int DEFAULT_SIZE = BoilerplateConstants.BUFF_SIZE_SMALL;
    private static final int POS_FLOAT_COUNT = 2;

    private float[] vertices;
    private int size;
    private boolean autoResize;

    private int floatCount = 0;
    private int vertexCount = 0;
    private int separationsCount = 0;

    private int additionalVertFloats = 0;
    private int floatCountPerVert = POS_FLOAT_COUNT;

    private boolean shouldNextBeSeparated = false;  // should the next time raw vertices are pushed be separated

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
        int newSize = BoilerplateConstants.findNextLargestBuffSize(minSize);
        if (newSize == BoilerplateConstants.ERROR) return BoilerplateConstants.ERROR;
        resizeBufferAndKeepElements(newSize);
        return size;
    }

    /**
     * PUSHING VERTICES & SHAPES
     */

    public void pushRawSeparatedVertices(float[] verts) {
        pushRawVertices(verts, true);
    }

    public void pushRawVertices(float[] verts) {
        pushRawVertices(verts, false);
    }

    public void pushRawVertices(float[] verts, boolean separation) {
        int fCount = verts.length;
        if (fCount == 0) return;

        if (floatCount + fCount > size) {
            if (!autoResize) {
                Logging.danger("Cannot add an additional '%s' items to an array at '%s' fullness, with '%s / %s' items already set. Aborting.",
                        fCount, getCurrentFullnessPercent(), floatCount, size);
                Logging.expensive("Consider setting autoResize to true! (or manually allow more space at the initialization of this StripBuilder)");
                return;
            }

            if (autoResizeBuffer(floatCount + fCount) == BoilerplateConstants.ERROR) {
                Logging.danger("An error occurred attempting to resize this buffer! Aborting.");
                return;
            }
        }

        // perform separation
        if (separation || shouldNextBeSeparated) {
            shouldNextBeSeparated = false;
            if (floatCount >= floatCountPerVert) {
                separationsCount++;
                float[] separationVerts = new float[floatCountPerVert * 2];  // pushing 2 vertices (last of current verts and first of new verts)
                System.arraycopy(vertices, floatCount - floatCountPerVert, separationVerts, 0, floatCountPerVert);
                System.arraycopy(verts, 0, separationVerts, floatCountPerVert, floatCountPerVert);
                pushRawVertices(separationVerts, false);  // FALSE!!!
            }
        }

        System.arraycopy(verts, 0, vertices, floatCount, fCount);
        floatCount += fCount;
        vertexCount += fCount / floatCountPerVert;
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

    private void addPointsToArray(float[] theArray, int floatInx, int vertInx, Vec2 point, ShapeMode mode) {
        theArray[floatInx] = point.x;
        theArray[floatInx+1] = point.y;

        if (mode instanceof ShapeMode.Demonstration demo) {
            Vec3 typeVar = demo.getVar(vertInx);
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
            Vec2 point = p.points.get(i);
            addPointsToArray(verts, inx, i, point.add(p.pos), p.mode);
        }
        pushRawVertices(verts);
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
            Vec2 point;
            if (i % 2 == 0) point = p.points.get(offset);  // front
            else point = p.points.get(p.points.size()-1 - offset);  // back
            addPointsToArray(verts, inx, i, point.add(p.pos), p.mode);
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
