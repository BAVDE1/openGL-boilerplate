package boilerplate.rendering.builders;

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
}
