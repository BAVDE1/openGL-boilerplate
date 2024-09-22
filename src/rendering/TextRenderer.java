package src.rendering;

import static org.lwjgl.opengl.GL11.*;
import src.game.Constants;
import src.utility.Vec2;
import src.utility.Vec2f;

import java.util.ArrayList;
import java.util.Arrays;

public class TextRenderer {
    public static class TextObject {
        public int index = 0;
        String string;
        Vec2f pos;

        int ySpacing = 10;

        public TextObject(String string, Vec2f pos) {
            this.string = string;
            this.pos = pos;
        }

        public float[] buildStrip() {
            StripBuilder2f sb = new StripBuilder2f();
            sb.setAdditionalVerts(0);  // todo: 2

            Vec2f charSize = new Vec2f(20, 30);  // todo: remove

            int accumulatedY = 0;
            for (String line : string.split("\n")) {
                if (line.isEmpty()) {
                    accumulatedY += (int) charSize.y + ySpacing;  // todo: just use height of 'A'
                    continue;
                }

                // add starting vert
                float lineY = pos.y + accumulatedY;
                sb.pushSeparatedVertices(new float[] {pos.x, lineY + charSize.y});

                // all chars
                int accumulatedX = 0;
                for (char c : line.toCharArray()) {
                    sb.pushVertices(new float[] {
                            pos.x + accumulatedX,              lineY,
                            pos.x + accumulatedX + charSize.x, lineY + charSize.y
                    });
                    accumulatedX += (int) charSize.x;
                }

                // add ending vert
                sb.pushVertices(new float[] {pos.x + accumulatedX, lineY});
                accumulatedY += (int) charSize.y + ySpacing;
            }

            return sb.getSetVertices();
        }
    }

    private final ArrayList<TextObject> textObjects = new ArrayList<>();

    VertexArray va;
    VertexBuffer vb;
    StripBuilder2f sb;

    int posSize = 2;
    int texCoordSize = 0;  // todo: 2

    int bufferSize;

    public TextRenderer() {this(Constants.BUFF_SIZE_GENERAL);}
    public TextRenderer(int size) {
        this.bufferSize = size;
    }

    /** after GL context created */
    public void setupBufferObjects() {
        va = new VertexArray();   va.genId();
        vb = new VertexBuffer();  vb.genId();
        sb = new StripBuilder2f(bufferSize);
        sb.setAdditionalVerts(texCoordSize);

        vb.bufferSize(bufferSize);

        VertexArray.VertexArrayLayout layout = new VertexArray.VertexArrayLayout();
        layout.pushFloat(posSize);  // position
        layout.pushFloat(texCoordSize);  // texture coord
        va.addBuffer(vb, layout);
    }

    public void addTextObject(TextObject to) {
        textObjects.add(to);
    }

    public void buildBuffer() {
        sb.clear();
        for (TextObject to : textObjects) {
            sb.pushSeparatedVertices(to.buildStrip());
        }

        Renderer.bindBuffer(vb);
        vb.BufferSubData(sb.getSetVertices());
    }

    public void draw() {
        Renderer.draw(GL_TRIANGLE_STRIP, va, sb.count / (posSize + texCoordSize));
    }
}
