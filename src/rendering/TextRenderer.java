package src.rendering;

import src.game.Constants;
import src.utility.Vec2;

public class TextRenderer {
    VertexArray va;
    VertexBuffer vb;
    StripBuilder2f sb;

    int size;
    int count = 0;

    public TextRenderer() {this(Constants.BUFF_SIZE_GENERAL);}
    public TextRenderer(int size) {
        this.size = size;
        setupBufferObjects();
    }

    private void setupBufferObjects() {
        va = new VertexArray();   va.genId();
        vb = new VertexBuffer();  vb.genId();
        sb = new StripBuilder2f(size);
        sb.setAdditionalVerts(2);

        vb.bufferSize(size);

        VertexArray.VertexArrayLayout layout = new VertexArray.VertexArrayLayout();
        layout.pushFloat(2);  // position
        layout.pushFloat(2);  // texture coord
        va.addBuffer(vb, layout);
    }

    // todo: font size
    public void pushString(String string, Vec2 pos) {
        // generate 2 tris and 2 texture coords for each letter
        sb.pushSeparatedVertices();
        vb.
        // add to count
    }

    public void draw() {

    }
}
