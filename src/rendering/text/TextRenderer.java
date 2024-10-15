package src.rendering.text;

import src.game.Constants;
import src.rendering.Renderer;
import src.rendering.StripBuilder2f;
import src.rendering.VertexArray;
import src.rendering.VertexBuffer;
import src.utility.Logging;
import src.utility.Vec2f;

import java.util.ArrayList;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;

public class TextRenderer {
    public static class TextObject {
        private TextRenderer parent;
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

        private void addParent(TextRenderer parent) {
            assert this.parent == null;
            this.parent = parent;
        }

        private void removeParent() {
            assert this.parent != null;
            this.parent = null;
        }

        private float[] buildStrip() {
            sb.clear();
            sb.setAdditionalVerts(3);

            FontManager.LoadedFont font = FontManager.getLoadedFont(loadedFontId);
            int genericHeight = (int) (font.glyphMap.get(' ').height * scale);

            int accumulatedY = 0;
            for (String line : string.split("\n")) {
                if (line.isEmpty()) {
                    accumulatedY += genericHeight + ySpacing;
                    continue;
                }

                // all chars in line
                float lineY = pos.y + accumulatedY;
                int accumulatedX = 0;
                for (char c : line.toCharArray()) {
                    if (!font.glyphMap.containsKey(c)) {
                        Logging.warn("Character '%s' does not exist in the currently loaded font. Using '0' instead.", c);
                        c = '0';
                    }

                    FontManager.Glyph glyph = font.glyphMap.get(c);
                    Vec2f size = new Vec2f(glyph.width, glyph.height).mul(scale);

                    // 2 4
                    // 1 3
                    float charX = pos.x + accumulatedX;
                    float[] charVertices = new float[] {
                            charX,          lineY + size.y, glyph.topLeft.x,     glyph.bottomRight.y, 0,
                            charX,          lineY,          glyph.topLeft.x,     glyph.topLeft.y,     0,
                            charX + size.x, lineY + size.y, glyph.bottomRight.x, glyph.bottomRight.y, 0,
                            charX + size.x, lineY,          glyph.bottomRight.x, glyph.topLeft.y,     0
                    };
                    if (accumulatedX == 0) sb.pushSeparatedVertices(charVertices);
                    else sb.pushVertices(charVertices);
                    accumulatedX += (int) size.x;
                }
                accumulatedY += genericHeight + ySpacing;
            }

            return sb.getSetVertices();
        }

        public void setString(String newString, Object... args) {
            setString(String.format(newString, args));
        }

        public void setString(String newString) {
            if (!Objects.equals(newString, string)) {
                string = newString;
                if (parent != null) parent.hasBeenModified = true;
            }
        }

        public String getString() {return string;}

        public void setPos(Vec2f newPos) {
            if (newPos != pos) {
                pos = newPos;
                if (parent != null) parent.hasBeenModified = true;
            }
        }

        public Vec2f getPos() {return pos;}

        public void setFontId(int newFontId) {
            if (newFontId != loadedFontId) {
                loadedFontId = newFontId;
                if (parent != null) parent.hasBeenModified = true;
            }
        }

        public int getLoadedFontId() {return loadedFontId;}

        public void setScale(float newScale) {
            if (newScale != scale) {
                scale = newScale;
                if (parent != null) parent.hasBeenModified = true;
            }
        }

        public float getScale() {return scale;}

        public void setYSpacing(int newYSpacing) {
            if (newYSpacing != ySpacing) {
                ySpacing = newYSpacing;
                if (parent != null) parent.hasBeenModified = true;
            }
        }

        public int getYSpacing() {return ySpacing;}
    }

    private final ArrayList<TextObject> textObjects = new ArrayList<>();

    private VertexArray va;
    private VertexBuffer vb;
    private StripBuilder2f sb;

    private int bufferSize = Constants.BUFF_SIZE_DEFAULT;
    private boolean hasBeenModified = false;

    public TextRenderer() {}
    public TextRenderer(int size) {this.bufferSize = size;}

    /** after GL context created */
    public void setupBufferObjects() {
        va = new VertexArray();   va.genId();
        vb = new VertexBuffer();  vb.genId();
        sb = new StripBuilder2f(bufferSize);

        sb.setAdditionalVerts(3);
        vb.bufferSize(bufferSize);

        VertexArray.VertexArrayLayout layout = new VertexArray.VertexArrayLayout();
        layout.setupDefaultLayout();
        va.addBuffer(vb, layout);
    }

    private void buildBuffer() {
        sb.clear();
        for (TextObject to : textObjects) {
            sb.pushSeparatedVertices(to.buildStrip());
        }

        Renderer.bindBuffer(vb);
        vb.bufferData(sb.getSetVertices());
        hasBeenModified = false;
    }

    public void draw() {
        if (hasBeenModified) buildBuffer();

        if (sb.count > 0) {
            Renderer.draw(GL_TRIANGLE_STRIP, va, sb.count / va.layout.getTotalItems());
        }
    }

    public ArrayList<TextObject> getTextObjects() {
        return textObjects;
    }

    public void pushTextObject(TextObject to) {
        to.addParent(this);
        textObjects.add(to);
        hasBeenModified = true;
    }

    public void removeTextObject(TextObject to) {
        to.removeParent();
        textObjects.remove(to);
        hasBeenModified = true;
    }

    public void clearAllTextObjects() {
        for (TextObject to : textObjects) to.removeParent();
        textObjects.clear();
        hasBeenModified = true;
    }
}
