package src.rendering.text;

import src.game.Constants;
import src.rendering.Shape;
import src.rendering.*;
import src.utility.Logging;
import src.utility.Vec2;

import java.awt.*;
import java.util.ArrayList;
import java.util.Objects;

import static org.lwjgl.opengl.GL11.GL_TRIANGLE_STRIP;

/**
 * Renders (multiple) TentRenderer.TextObjects in its own auto-resizing buffer.
 * TextRenderer buffer is only rebuilt when one of its TextObjects has been modified
 */
public class TextRenderer {
    public static class TextObject {
        private TextRenderer parent;
        private float scale = 1;
        private int ySpacing = 5;

        private int loadedFontId;
        private String string;
        private Vec2 pos;

        private final BufferBuilder2f sb = new BufferBuilder2f(true);
        private boolean hasChanged = true;

        private Color bgColour = new Color(0, 0, 0, 0);
        private boolean seamlessBg = false;

        public TextObject(int loadedFontId, String string, Vec2 pos, float scale, int ySpacing) {
            this(loadedFontId, string, pos);
            this.scale = scale;
            this.ySpacing = ySpacing;
        }

        public TextObject(int loadedFontId, String string, Vec2 pos) {
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
            if (!hasChanged) return sb.getSetVertices();  // don't even bother re-building

            sb.clear();
            sb.setAdditionalVerts(VertexArray.Layout.defaultLayoutAdditionalVerts());

            FontManager.LoadedFont font = FontManager.getLoadedFont(loadedFontId);
            int genericHeight = (int) (font.glyphMap.get(' ').height * scale);
            int yAddition = genericHeight + ySpacing;

            int accumulatedY = 0;
            for (String line : string.split("\n")) {
                if (line.isEmpty()) {
                    accumulatedY += genericHeight + ySpacing;
                    continue;
                }

                float lineY = pos.y + accumulatedY;
                int accumulatedX = 0;

                // line background
                if (bgColour.getAlpha() > Constants.EPSILON) {
                    Vec2 topLeft = new Vec2(pos.x, lineY);
                    Vec2 size = new Vec2(font.findLineWidth(line) * scale, yAddition);
                    if (!seamlessBg) size.y -= ySpacing;

                    sb.pushSeparatedQuad(Shape.createRect(topLeft, size, new Shape.Mode(bgColour)));
                }

                // all chars in line
                for (char c : line.toCharArray()) {
                    if (!font.glyphMap.containsKey(c)) {
                        Logging.warn("Character '%s' does not exist in the currently loaded font. Using '0' instead.", c);
                        c = '0';
                    }

                    FontManager.Glyph glyph = font.glyphMap.get(c);
                    Vec2 size = new Vec2(glyph.width, glyph.height).mul(scale);
                    Vec2 topLeft = new Vec2(pos.x + accumulatedX, lineY);

                    Shape.Mode mode = new Shape.Mode(FontManager.FONT_TEXTURE_SLOT, glyph.texTopLeft, glyph.texSize);
                    Shape.Quad quad = Shape.createRect(topLeft, size, mode);
                    if (accumulatedX == 0) sb.pushSeparatedQuad(quad);
                    else sb.pushQuad(quad);

                    accumulatedX += (int) size.x;
                }
                accumulatedY += yAddition;
            }

            hasChanged = false;
            return sb.getSetVertices();
        }

        public String getString() {return string;}
        public void setString(String newString, Object... args) {
            setString(String.format(newString, args));
        }

        public void setString(String newString) {
            if (!Objects.equals(newString, string)) {
                string = newString;
                setHasChanged();
            }
        }

        public Vec2 getPos() {return pos.getClone();}
        public void setPos(Vec2 newPos) {
            if (newPos != pos) {
                pos = newPos;
                setHasChanged();
            }
        }

        public int getLoadedFontId() {return loadedFontId;}
        public void setFontId(int newFontId) {
            if (newFontId != loadedFontId) {
                loadedFontId = newFontId;
                setHasChanged();
            }
        }

        public float getScale() {return scale;}
        public void setScale(float newScale) {
            if (newScale != scale) {
                scale = newScale;
                setHasChanged();
            }
        }

        public int getYSpacing() {return ySpacing;}
        public void setYSpacing(int newYSpacing) {
            if (newYSpacing != ySpacing) {
                ySpacing = newYSpacing;
                setHasChanged();
            }
        }

        public Color getBgColour() {return bgColour;}
        public void setBgColour(Color newBgColour) {
            if (newBgColour != bgColour) {
                bgColour = newBgColour;
                setHasChanged();
            }
        }

        public boolean getSeamlessBg() {return seamlessBg;}
        public void setSeamlessBg(boolean isSeamlessBg) {
            if (isSeamlessBg != seamlessBg) {
                seamlessBg = isSeamlessBg;
                setHasChanged();
            }
        }

        private void setHasChanged() {
            hasChanged = true;
            if (parent != null) parent.hasBeenModified = true;
        }
    }

    private final ArrayList<TextObject> textObjects = new ArrayList<>();

    private VertexArray va;
    private VertexBuffer vb;
    private BufferBuilder2f sb;

    private boolean hasBeenModified = false;

    /** after GL context created */
    public void setupBufferObjects() {
        va = new VertexArray();   va.genId();
        vb = new VertexBuffer();  vb.genId();
        sb = new BufferBuilder2f(true);

        sb.setAdditionalVerts(VertexArray.Layout.defaultLayoutAdditionalVerts());
        va.pushBuffer(vb, VertexArray.Layout.getDefaultLayout());
    }

    private void buildBuffer() {
        sb.clear();

        for (TextObject to : textObjects) {
            if (to.string.isEmpty() || to.scale < Constants.EPSILON) continue;
            sb.pushRawSeparatedVertices(to.buildStrip());
        }

        Renderer.bindBuffer(vb);
        vb.bufferData(sb.getSetVertices());
        hasBeenModified = false;
    }

    public void draw() {
        if (hasBeenModified) buildBuffer();

        if (sb.getFloatCount() > 0) {
            Renderer.draw(GL_TRIANGLE_STRIP, va, sb.getVertexCount());
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

    public BufferBuilder2f getBufferBuilder() {
        return sb;
    }
}
