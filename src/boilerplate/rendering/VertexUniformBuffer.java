package boilerplate.rendering;

import boilerplate.utility.Logging;
import org.lwjgl.opengl.GL45;

import java.util.HashMap;

public class VertexUniformBuffer extends VertexBuffer {
    private static int blockBindingCounter = 0;
    private static final HashMap<String, Integer> blockBindings = new HashMap<>();
    int blockBinding = -1;

    public VertexUniformBuffer() {
        this.bufferType = GL45.GL_UNIFORM_BUFFER;
    }

    public VertexUniformBuffer(boolean genId) {
        this();
        if (genId) genId();
    }

    public void bindUniformBlock(ShaderProgram sh, String uniformBlock) {
        if (!blockBindings.containsKey(uniformBlock)) blockBindings.put(uniformBlock, blockBindingCounter++);
        int blockBinding = blockBindings.get(uniformBlock);
        int blockInx = sh.getUniformBlockLocation(uniformBlock);

        GL45.glUniformBlockBinding(sh.getProgram(), blockInx, blockBinding);
        if (this.blockBinding < 0) bindBufferToBlock(blockBinding);
    }

    private void bindBufferToBlock(int blockBinding) {
        this.blockBinding = blockBinding;
        GL45.glBindBufferBase(bufferType, blockBinding, bufferId);
    }

    @Override
    public void setBufferType(int bufferType) {
        Logging.danger("Cannot set buffer type on this object. Use a VertexBuffer instead.");
    }
}
