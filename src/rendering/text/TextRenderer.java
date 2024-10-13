package src.rendering.text;

import static org.lwjgl.opengl.GL11.*;
import src.game.Constants;
import src.rendering.Renderer;
import src.rendering.StripBuilder2f;
import src.rendering.VertexArray;
import src.rendering.VertexBuffer;
import src.utility.Logging;
import src.utility.Vec2f;

import javax.annotation.processing.SupportedSourceVersion;
import java.util.ArrayList;

public class TextRenderer {
    public static class TextObject {
        private float scale = 1;
        private int ySpacing = 5;

        private int loadedFontId;
        private String string;
        private Vec2f pos;

        private final StripBuilder2f sb = new StripBuilder2f();

        public TextObject(int loadedFontId, String string, Vec2f pos, float scale, int ySpacing) {
            this(loadedFontId, string, pos);
            this.scale = scale;
            this.ySpacing = ySpacing;
        }
        public TextObject(int loadedFontId, String string, Vec2f pos) {
            this.loadedFontId = loadedFontId;
            this.string = string;
            this.pos = pos;
        }

        public float[] buildStrip(int additionalVerts) {
            sb.clear();
            sb.setAdditionalVerts(additionalVerts);

            FontManager.LoadedFont font = FontManager.getLoadedFont(loadedFontId);
            FontManager.Glyph spaceGlyph = font.glyphMap.get(' ');

            int accumulatedY = 0;
            for (String line : string.split("\n")) {
                if (line.isEmpty()) {
                    accumulatedY += spaceGlyph.height + ySpacing;
                    continue;
                }

                // all chars in line
                float lineY = pos.y + accumulatedY;
                int accumulatedX = 0;
                for (char c : line.toCharArray()) {
                    if (!font.glyphMap.containsKey(c)) {
                        Logging.danger("Character '%s' does not exist in the currently loaded font. Using '0' instead.", c);
                        c = '0';
                    }

                    FontManager.Glyph glyph = font.glyphMap.get(c);

                    // 2 4
                    // 1 3
                    sb.pushSeparatedVertices(new float[] {
                            pos.x + accumulatedX,               lineY + glyph.height, 0, 0, -1,
                            pos.x + accumulatedX,               lineY,                0, 0, -1,
                            pos.x + accumulatedX + glyph.width, lineY + glyph.height, 0, 0, -1,
                            pos.x + accumulatedX + glyph.width, lineY,                0, 0, -1
                    });
                    accumulatedX += glyph.width;
                }

                accumulatedY += spaceGlyph.height + ySpacing;
            }

            return sb.getSetVertices();
        }
    }

    private final ArrayList<TextObject> textObjects = new ArrayList<>();

    private VertexArray va;
    private VertexBuffer vb;
    private StripBuilder2f sb;

    private int bufferSize = Constants.BUFF_SIZE_DEFAULT;
    private final int posVertsCount = 2;
    private final int texCoordCount = 2;
    private final int slotCount = 1;
    private boolean hasBeenModified = false;

    public TextRenderer() {}
    public TextRenderer(int size) {this.bufferSize = size;}

    /** after GL context created */
    public void setupBufferObjects() {
        va = new VertexArray();   va.genId();
        vb = new VertexBuffer();  vb.genId();
        sb = new StripBuilder2f(bufferSize);
        sb.setAdditionalVerts(texCoordCount + slotCount);

        vb.bufferSize(bufferSize);

        VertexArray.VertexArrayLayout layout = new VertexArray.VertexArrayLayout();
        layout.pushFloat(posVertsCount);  // pos
        layout.pushFloat(texCoordCount);  // tex coord
        layout.pushFloat(slotCount);  // slot
        va.addBuffer(vb, layout);
    }

    private void buildBuffer() {
        sb.clear();
        for (TextObject to : textObjects) {
            sb.pushSeparatedVertices(to.buildStrip(texCoordCount + slotCount));
        }

        Renderer.bindBuffer(vb);
        vb.bufferData(sb.getSetVertices());
        hasBeenModified = false;
        System.out.println("build");
    }

    public void draw() {
        if (hasBeenModified) buildBuffer();

        if (sb.count > 0) {
            Renderer.draw(GL_TRIANGLE_STRIP, va, sb.count / va.layout.getTotalItems());
        }
    }

    public void pushTextObject(TextObject to) {
        textObjects.add(to);
        hasBeenModified = true;
    }

    public void removeTextObject(TextObject to) {
        textObjects.remove(to);
        hasBeenModified = true;
    }

    public void clearAllTextObjects() {
        textObjects.clear();
        hasBeenModified = true;
    }

    public void setString(TextObject to, String newString) {
        to.string = newString;
        hasBeenModified = true;
    }

    public void setPos(TextObject to, Vec2f newPos) {
        to.pos = newPos;
        hasBeenModified = true;
    }

    public void setFontId(TextObject to, int newFontId) {
        to.loadedFontId = newFontId;
        hasBeenModified = true;
    }

    public void setScale(TextObject to, float newScale) {
        to.scale = newScale;
        hasBeenModified = true;
    }

    public void setYSpacing(TextObject to, int newYSpacing) {
        to.ySpacing = newYSpacing;
        hasBeenModified = true;
    }
}
