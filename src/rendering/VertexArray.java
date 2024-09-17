package src.rendering;

import org.lwjgl.opengl.GL45;
import src.utility.Logging;

import java.util.ArrayList;

import static org.lwjgl.opengl.GL11.*;


public class VertexArray {
    public class VertexBufferElement {
        public int type;
        public int count;
        public boolean normalized;

        public VertexBufferElement(int type, int count, boolean normalized) {
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

    public class VertexBufferLayout {
        private final ArrayList<VertexBufferElement> elements = new ArrayList<>();
        private int stride = 0;

        public VertexBufferLayout(){}

        public void push(int type, int count, boolean normalized) {
            elements.add(new VertexBufferElement(type, count, normalized));
            stride += count * VertexBufferElement.getByteSizeForType(type);
        }

        public void pushFloat(int count) {
            push(GL_FLOAT, count, false);
        }

        public void pushInt(int count) {
            push(GL_UNSIGNED_INT, count, false);
        }

        public ArrayList<VertexBufferElement> getElements() {
            return elements;
        }
    }

    public Integer arrayId;
    public int attribCount = 0;

    public VertexArray(){}

    public void genId() {
        if (arrayId != null) {
            Logging.warn("Attempting to re-generate already generated array id, aborting");
            return;
        }
        arrayId = GL45.glGenVertexArrays();
    }

    public void addBuffer(VertexBuffer vb, VertexBufferLayout layout) {
        vb.bind();

        ArrayList<VertexBufferElement> allElements = layout.getElements();
        int offset = 0;

        for (int i = 0; i < allElements.size(); i++) {
            VertexBufferElement element = allElements.get(i);
            GL45.glEnableVertexAttribArray(i);
            GL45.glVertexAttribPointer(i, element.count, element.type, element.normalized, layout.stride, offset);
            offset += element.count * element.getByteSizeForType();
            attribCount++;
        }
    }

    public void bind() {
        GL45.glBindVertexArray(arrayId);
    }

    public void unbind() {
        GL45.glBindVertexArray(0);
    }
}
