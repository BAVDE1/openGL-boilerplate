package boilerplate.rendering.buffers;

import boilerplate.utility.Logging;
import org.lwjgl.opengl.GL45;

/**
 * Frame Buffer is a section of the GPUs memory that holds buffers (colour buffer, depth buffer, stencil buffer).
 * A default frame buffer is created by GLFW.
 */
public class FrameBuffer {
    protected Integer bufferId;

    public FrameBuffer() {
    }

    public FrameBuffer(boolean genId) {

    }

    public int getId() {
        return bufferId;
    }
}
