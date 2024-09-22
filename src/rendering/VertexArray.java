package src.rendering;

import org.lwjgl.opengl.GL45;
import src.utility.Logging;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL45.*;


public class VertexArray {
    public static class VertexArrayElement {
        public int type;
        public int count;
        public boolean normalized;

        public VertexArrayElement(int type, int count, boolean normalized) {
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

    public static class VertexArrayLayout {
        private final ArrayList<VertexArrayElement> elements = new ArrayList<>();
        private int stride = 0;

        public VertexArrayLayout(){}

        private void push(int type, int count, boolean normalized) {
            elements.add(new VertexArrayElement(type, count, normalized));
            stride += count * VertexArrayElement.getByteSizeForType(type);
        }

        public void pushFloat(int count) {
            if (count == 0) return;
            push(GL_FLOAT, count, false);
        }

        public void pushInt(int count) {
            if (count == 0) return;
            push(GL_UNSIGNED_INT, count, false);
        }

        public ArrayList<VertexArrayElement> getElements() {
            return elements;
        }
    }

    private Integer arrayId;
    public int attribCount = 0;

    public VertexArray(){}

    public void genId() {
        if (arrayId != null) {
            Logging.warn("Attempting to re-generate already generated array id, aborting");
            return;
        }
        arrayId = glGenVertexArrays();
    }

    public void addBuffer(VertexBuffer vb, VertexArrayLayout layout) {
        Renderer.bindArray(this);
        Renderer.bindBuffer(vb);

        int offset = 0;
        ArrayList<VertexArrayElement> allElements = layout.getElements();
        for (int i = 0; i < allElements.size(); i++) {
            VertexArrayElement element = allElements.get(i);
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
