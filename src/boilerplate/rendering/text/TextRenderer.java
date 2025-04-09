package boilerplate.rendering.text;

import boilerplate.common.BoilerplateConstants;
import boilerplate.rendering.Shape2d;
import boilerplate.rendering.*;
import boilerplate.utility.Logging;
import boilerplate.utility.Vec2;

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
        public static final int ALIGN_LEFT = 0;
        public static final int ALIGN_RIGHT = 1;
        public static final int ALIGN_MIDDLE = 2;

        private TextRenderer parent;
        private Color textColour = Color.WHITE;
        private float scale = 1;
        private int ySpacing = 0;
        private int alignment = ALIGN_LEFT;

        private int loadedFontId;
        private String string;
        private Vec2 pos;

        private final BufferBuilder2f sb = new BufferBuilder2f(true, FontManager.textLayoutAdditionalVerts());
        private final BufferBuilder2f bgSb = new BufferBuilder2f(true, FontManager.textLayoutAdditionalVerts());
        private boolean hasChanged = true;

        private Color bgCol = new Color(0, 0, 0, 0);
        private final Vec2 bgMargin = new Vec2();
        private boolean seamlessBgLines = false;

        public TextObject(int loadedFontId, String string, Vec2 pos, float scale, int ySpacing) {
            this(loadedFontId, string, pos);
            setScale(scale);
            setYSpacing(ySpacing);
        }

        public TextObject(int loadedFontId, String string, Vec2 pos, Color textColour, Color bgCol) {
            this(loadedFontId, string, pos);
            setTextColour(textColour);
            setBgCol(bgCol);
        }

        public TextObject(int loadedFontId, String string, Vec2 pos) {
            setLoadedFontId(loadedFontId);
            setString(string);
            setPos(pos);
        }

        private void addParent(TextRenderer parent) {
            if (this.parent != null) {
                Logging.danger("parent is already assigned to this text object, aborting");
                return;
            }
            this.parent = parent;
        }

        private void removeParent() {
            if (this.parent == null) return;
            this.parent = null;
        }

        private float[] buildStrip() {
            if (!hasChanged) return sb.getSetVertices();  // don't even bother re-building

            sb.clear();
            bgSb.clear();

            FontManager.LoadedFont font = FontManager.getLoadedFont(loadedFontId);
            int genericHeight = (int) (font.getLineHeight() * scale);
            int yAddition = genericHeight + ySpacing;

            String[] lines = string.split("\n");

            int accumulatedY = 0;
            for (String line : lines) {
                if (line.isEmpty()) {
                    accumulatedY += genericHeight + ySpacing;
                    continue;
                }

                int accumulatedX = 0;
                float lineWidth = font.findLineWidth(line) * scale;
                Vec2 linePos = new Vec2(alignment == 0 ? pos.x : pos.x - (lineWidth * (1f / alignment)), pos.y + accumulatedY);

                // line background
                if (bgCol.getAlpha() > BoilerplateConstants.EPSILON) {
                    Vec2 size = new Vec2(lineWidth, yAddition);
                    if (!seamlessBgLines) size.y -= ySpacing;

                    float[] color = new float[] {-1, -1, bgCol.getRed(), bgCol.getGreen(), bgCol.getBlue(), bgCol.getAlpha()};
                    Shape2d.Poly p = Shape2d.createRect(linePos.sub(bgMargin), size.add(bgMargin.mul(2)), new ShapeMode.Append(color));
                    bgSb.pushSeparatedPolygon(p);
                }

                // all chars in line
                for (char c : line.toCharArray()) {
                    FontManager.Glyph glyph = font.getGlyph(c);
                    Vec2 size = glyph.getSize();
                    Vec2 topLeft = linePos.add(accumulatedX, 0);

                    Shape2d.Poly texturePoints = Shape2d.createRect(glyph.texTopLeft, glyph.texSize);
                    float[] colorVars = new float[] {textColour.getRed(), textColour.getGreen(), textColour.getBlue(), textColour.getAlpha()};

                    ShapeMode.UnpackAppend mode = new ShapeMode.UnpackAppend(texturePoints.toArray(), colorVars);
                    Shape2d.Poly p = Shape2d.createRect(topLeft, size, mode);

                    if (accumulatedX == 0) sb.pushSeparatedPolygon(p);
                    else sb.pushPolygon(p);
                    accumulatedX += (int) size.x;
                }
                accumulatedY += yAddition;
            }

            hasChanged = false;

            sb.prependBuffer(bgSb, true);
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
        public void setLoadedFontId(int newFontId) {
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

        public Color getTextColour() {return textColour;}
        public void setTextColour(Color newColour) {
            if (!newColour.equals(textColour)) {
                textColour = newColour;
                setHasChanged();
            }
        }

        public Color getBgCol() {return bgCol;}
        public void setBgCol(Color newBgColour) {
            if (newBgColour != bgCol) {
                bgCol = newBgColour;
                setHasChanged();
            }
        }

        public boolean getSeamlessBgLines() {return seamlessBgLines;}
        public void setSeamlessBgLines(boolean isSeamlessBg) {
            if (isSeamlessBg != seamlessBgLines) {
                seamlessBgLines = isSeamlessBg;
                setHasChanged();
            }
        }

        public int getAlignment() {return alignment;}
        public void setAlignment(int newAlignment) {
            if (newAlignment < ALIGN_LEFT || newAlignment > ALIGN_MIDDLE) {
                Logging.warn("Alignment '%s' is not valid", newAlignment);
                return;
            }

            if (newAlignment != alignment) {
                alignment = newAlignment;
                setHasChanged();
            }
        }

        public Vec2 getBgMargin() {return bgMargin;}
        public void setBgMargin(Vec2 newBgMargin) {
            if (!newBgMargin.equals(bgMargin)) {
                bgMargin.set(newBgMargin);
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
        va = new VertexArray(true);
        vb = new VertexBuffer(true);
        sb = new BufferBuilder2f(true, FontManager.textLayoutAdditionalVerts());

        va.pushBuffer(vb, FontManager.getTextVertexLayout());
    }

    private void buildBuffer() {
        sb.clear();

        for (TextObject to : textObjects) {
            if (to.string.isEmpty() || to.scale < BoilerplateConstants.EPSILON) continue;
            sb.pushRawSeparatedVertices(to.buildStrip());
        }

        vb.bufferSetData(sb);
        hasBeenModified = false;
    }

    public void draw() {
        if (hasBeenModified) buildBuffer();

        if (sb.getFloatCount() > 0) {
            FontManager.bindTextShader();
            Renderer.draw(GL_TRIANGLE_STRIP, va, sb.getVertexCount());
        }
    }

    public ArrayList<TextObject> getTextObjects() {
        return textObjects;
    }

    public void pushTextObject(TextObject... tos) {
        for (TextObject to : tos) {
            to.addParent(this);
            textObjects.add(to);
            hasBeenModified = true;
        }
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
