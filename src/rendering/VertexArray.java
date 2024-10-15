package src.rendering;

import src.utility.Logging;

import java.util.ArrayList;
import java.util.Arrays;

import static org.lwjgl.opengl.GL45.*;


public class VertexArray {
    public static class Element {
        public int type;
        public int count;
        public boolean normalized;

        public Element(int type, int count, boolean normalized) {
            this.type = type;
            this.count = count;
            this.normalized = normalized;
        }

        public int getByteSizeForType() {
            return getByteSizeForType(type);
        }

        public static int getByteSizeForType(int type) {
            return switch (type) {
                case (GL_FLOAT) -> Float.BYTES;
                case (GL_UNSIGNED_INT) -> Integer.BYTES;
                default -> {
                    Logging.danger("Can't find case for type '%s', no bytes returned");
                    yield 0;
                }
            };
        }
    }

    public static class Layout {
        private final static int[] defaultLayout = new int[] {2, 2, 1};  // 2x pos, 2x texCoord, 1x slot

        private final ArrayList<Element> elements = new ArrayList<>();
        private int totalItems = 0;
        private int stride = 0;

        private void push(int type, int count, boolean normalized) {
            elements.add(new Element(type, count, normalized));
            stride += count * Element.getByteSizeForType(type);
            totalItems += count;
        }

        public void pushInt(int count) {
            if (count == 0) return;
            push(GL_FLOAT, count, false);
        }

        public ArrayList<Element> getElements() {
            return elements;
        }

        public int getTotalItems() {
            return totalItems;
        }

        /** Push the default vertex layout: 2 pos, 2 texCoords, 1 slot */
        public void setupDefaultLayout() {
            for (int f : defaultLayout) {
                pushInt(f);
            }
        }

        public static Layout getDefaultLayout() {
            Layout l = new Layout();
            l.setupDefaultLayout();
            return l;
        }

        /** returns the additional verts to add for the default layout */
        public static int defaultLayoutAdditionalVerts() {
            return defaultLayoutTotalFloatCount() - defaultLayout[0];
        }

        /** returns the number of float in 1 vertex from the default layout */
        public static int defaultLayoutTotalFloatCount() {
            return Arrays.stream(defaultLayout).sum();
        }
    }

    public Layout layout;
    private Integer arrayId;
    public int attribCount = 0;

    public void genId() {
        if (arrayId != null) {
            Logging.warn("Attempting to re-generate already generated array id, aborting");
            return;
        }
        arrayId = glGenVertexArrays();
    }

    public void addBuffer(VertexBuffer vb, Layout layout) {
        this.layout = layout;

        Renderer.bindArray(this);
        Renderer.bindBuffer(vb);

        int offset = 0;
        ArrayList<Element> allElements = layout.getElements();
        for (int i = 0; i < allElements.size(); i++) {
            Element element = allElements.get(i);
            glEnableVertexAttribArray(i);
            glVertexAttribPointer(i, element.count, element.type, element.normalized, layout.stride, offset);

            offset += element.count * element.getByteSizeForType();
            attribCount++;
        }
    }

    public int getId() {
        return arrayId;
    }
}
