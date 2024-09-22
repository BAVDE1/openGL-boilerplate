package src.rendering;

import static org.lwjgl.opengl.GL11.*;
import src.game.Constants;
import src.utility.Logging;
import src.utility.Vec2f;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class TextRenderer {
    public static class TextObject {
        private Font font;
        public float fontSize = 25;
        public int fontStyle = Font.PLAIN;

        public int index = -1;
        String string;
        Vec2f pos;

        int ySpacing = 10;

        // todo: different sizes
        public TextObject(String string, Vec2f pos) {
            this.string = string;
            this.pos = pos;

            prepareFont(Constants.DEFAULT_FONT);
        }

        public void prepareFont(String fontFile) {
            try {
                font = Font.createFont(Font.TRUETYPE_FONT, new File(fontFile));
                font.deriveFont(fontStyle, fontSize);
            } catch (IOException | FontFormatException e) {
                Logging.danger("Error preparing font, aborting. Thrown message:\n%s", e);
            }
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

                // starting vert
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

                // ending vert
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

    int posNumCount = 2;
    int texCoordNumCount = 0;  // todo: 2

    int bufferSize;

    public TextRenderer() {this(Constants.BUFF_SIZE_DEFAULT);}
    public TextRenderer(int size) {
        this.bufferSize = size;
    }

    /** after GL context created */
    public void setupBufferObjects() {
        va = new VertexArray();   va.genId();
        vb = new VertexBuffer();  vb.genId();
        sb = new StripBuilder2f(bufferSize);
        sb.setAdditionalVerts(texCoordNumCount);

        vb.bufferSize(bufferSize);

        VertexArray.VertexArrayLayout layout = new VertexArray.VertexArrayLayout();
        layout.pushFloat(posNumCount);  // position
        layout.pushFloat(texCoordNumCount);  // texture coord
        va.addBuffer(vb, layout);
    }

    public void addTextObject(TextObject to) {
        textObjects.add(to);
        to.index = textObjects.size() - 1;
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
        Renderer.draw(GL_TRIANGLE_STRIP, va, sb.count / va.layout.getTotalItems());
    }
}
