package src.rendering;

import src.game.Constants;
import src.utility.Vec2;

import java.util.ArrayList;

public class TextRenderer {
    public static class TextObject {
        public int index = 0;
        String string;
        Vec2 pos;

        public TextObject(String string, Vec2 pos) {
            this.string = string;
            this.pos = pos;
        }
    }

    private final ArrayList<TextObject> textObjects = new ArrayList<>();
    VertexArray va;
    VertexBuffer vb;
    StripBuilder2f sb;

    int bufferSize;

    public TextRenderer() {this(Constants.BUFF_SIZE_GENERAL);}
    public TextRenderer(int size) {
        this.bufferSize = size;
        setupBufferObjects();
    }

    private void setupBufferObjects() {
        va = new VertexArray();   va.genId();
        vb = new VertexBuffer();  vb.genId();
        sb = new StripBuilder2f(bufferSize);
        sb.setAdditionalVerts(2);

        vb.bufferSize(bufferSize);

        VertexArray.VertexArrayLayout layout = new VertexArray.VertexArrayLayout();
        layout.pushFloat(2);  // position
        layout.pushFloat(2);  // texture coord
        va.addBuffer(vb, layout);
    }

    public void addTextObject(TextObject to) {

    }

    // todo: font size
    public void pushString(String string, Vec2 pos) {
        // generate 2 tris and 2 texture coords for each letter
//        sb.pushSeparatedVertices();
//        vb.
        // add to count
    }

    public void draw() {

    }
}
