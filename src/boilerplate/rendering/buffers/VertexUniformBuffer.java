package boilerplate.rendering.buffers;

import boilerplate.rendering.ShaderProgram;
import boilerplate.utility.Logging;
import org.lwjgl.opengl.GL45;

import java.util.HashMap;

/**
 * Used for Uniform Blocks
 * You'll need to keep track of storage & uniform locations withing the block manually
 * <p>
 * N = 4 bytes.
 * scalar (e.g. int, bool): N.
 * vector: 2N or 4N (so vec3 & vec4 will have 4N).
 * array: each element base alignment equal to vec4.
 * matrix: array of column vectors (each vector is vec4) (e.g. mat4x4 = N * 4 column * 4 rows)
 */
public class VertexUniformBuffer extends VertexBuffer {
    private static int blockBindingCounter = 0;
    private static final HashMap<String, Integer> blockBindings = new HashMap<>();
    Integer blockBinding;

    public VertexUniformBuffer() {
        this.bufferType = GL45.GL_UNIFORM_BUFFER;
    }

    public VertexUniformBuffer(boolean generateId) {
        this();
        if (generateId) genId();
    }

    public void bindUniformBlock(String uniformBlock, ShaderProgram... programs) {
        if (!blockBindings.containsKey(uniformBlock)) blockBindings.put(uniformBlock, blockBindingCounter++);
        int blockBinding = blockBindings.get(uniformBlock);
        if (this.blockBinding == null) bindBufferToBlock(blockBinding);

        for (ShaderProgram sh : programs) {
            int blockInx = sh.getUniformBlockLocation(uniformBlock);
            GL45.glUniformBlockBinding(sh.getProgram(), blockInx, blockBinding);
        }
    }

    private void bindBufferToBlock(int blockBinding) {
        this.blockBinding = blockBinding;
        GL45.glBindBufferBase(bufferType, blockBinding, bufferId);
    }

    public static void unbind() {
        unbindTYpe(GL45.GL_UNIFORM_BUFFER);
    }

    @Override
    public void setBufferType(int bufferType) {
        Logging.danger("Cannot set buffer type on this object. Use a VertexBuffer instead.");
    }
}
