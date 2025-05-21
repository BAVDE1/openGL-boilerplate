package boilerplate.rendering.buffers;

import boilerplate.rendering.Texture;
import boilerplate.utility.Logging;
import org.lwjgl.opengl.GL45;

import java.awt.*;
import java.util.ArrayList;

/**
 * Frame Buffer is a section of the GPUs memory that holds buffers (colour buffer, depth buffer, stencil buffer).
 * A default frame buffer is created by GLFW.
 * Custom Frame Buffers can be used for, say, mirrors or post-processing and the like.
 */
public class FrameBuffer {
    protected Integer bufferId;

    public int DEFAULT_COLOUR_BUFF_FORMAT = GL45.GL_RGBA;

    protected ArrayList<Texture> colourBuffersAttached = new ArrayList<>();
    protected Texture depthBuffer;
    protected Texture stencilBuffer;
    protected Texture depthStencilBuffer;

    public FrameBuffer() {

    }

    public FrameBuffer(boolean genId) {
        if (genId) genId();
    }

    public Texture createDefaultColourBuffer(Dimension size) {
        return createTextureBuffer(size, DEFAULT_COLOUR_BUFF_FORMAT, DEFAULT_COLOUR_BUFF_FORMAT);
    }

    public void attachColourBuffer(Texture colourBuff) {
        GL45.glFramebufferTexture2D(GL45.GL_FRAMEBUFFER, GL45.GL_COLOR_ATTACHMENT0 + colourBuffersAttached.size(), GL45.GL_TEXTURE_2D, colourBuff.getId(), 0);
        colourBuffersAttached.add(colourBuff);
    }

    public Texture createDefaultDepthBuffer(Dimension size) {
        return createTextureBuffer(size, GL45.GL_DEPTH_COMPONENT, GL45.GL_DEPTH_COMPONENT);
    }

    public void attachDepthBuffer(Texture depthBuff) {
        depthBuffer = depthBuff;
        GL45.glFramebufferTexture2D(GL45.GL_FRAMEBUFFER, GL45.GL_DEPTH_ATTACHMENT, GL45.GL_TEXTURE_2D, depthBuff.getId(), 0);
    }

    public Texture createDefaultStencilBuffer(Dimension size) {
        return createTextureBuffer(size, GL45.GL_STENCIL_INDEX, GL45.GL_STENCIL_INDEX);
    }

    public void attachStencilBuffer(Texture stencilBuff) {
        depthBuffer = stencilBuff;
        GL45.glFramebufferTexture2D(GL45.GL_FRAMEBUFFER, GL45.GL_STENCIL_ATTACHMENT, GL45.GL_TEXTURE_2D, stencilBuff.getId(), 0);
    }

    public Texture createDefaultDepthStencilBuffer(Dimension size) {
        Texture buff = createTextureBuffer(size, GL45.GL_DEPTH24_STENCIL8, GL45.GL_DEPTH_STENCIL);
        buff.TEXTURE_TYPE = GL45.GL_UNSIGNED_INT_24_8;
        return buff;
    }

    public void attachDepthStencilBuffer(Texture depthStencilBuff) {
        this.depthStencilBuffer = depthStencilBuff;
        GL45.glFramebufferTexture2D(GL45.GL_FRAMEBUFFER, GL45.GL_DEPTH_STENCIL_ATTACHMENT, GL45.GL_TEXTURE_2D, depthStencilBuff.getId(), 0);
    }

    public Texture createTextureBuffer(Dimension size, int storedFormat, int givenFormat) {
        Texture buff = new Texture(size, true);
        buff.bind();
        buff.createTexture(storedFormat, givenFormat, null);
        return buff;
    }

    public void genId() {
        if (bufferId != null) {
            Logging.warn("Attempting to re-generate already generated frame buffer id, aborting");
            return;
        }
        bufferId = GL45.glGenFramebuffers();
    }

    public int getId() {
        return bufferId;
    }

    public void bind() {
        GL45.glBindFramebuffer(GL45.GL_FRAMEBUFFER, bufferId);
    }

    /**
     * 0 reverts to use the default frame buffer, set by the windowing system (GLFW)
     */
    public void unbind() {
        GL45.glBindFramebuffer(GL45.GL_FRAMEBUFFER, 0);
    }

    public void delete() {
        GL45.glDeleteFramebuffers(bufferId);
    }

    public boolean isCompletelyBuild() {
        bind();
        return GL45.glCheckFramebufferStatus(GL45.GL_FRAMEBUFFER) == GL45.GL_FRAMEBUFFER_COMPLETE;
    }
}
