package src.rendering;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import src.utility.Logging;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load;

public class Texture {
    static final ArrayList<Integer> boundSlots = new ArrayList<>();
    static final int BPP = 4;  // bytes per pixel

    private int texId;
    int width, height;

    public Texture(String filePath) {
        ByteBuffer buffer;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Prepare image buffers
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            buffer = stbi_load(filePath, w, h, comp, BPP);
            if (buffer == null) {
                Logging.danger("Failed to load a texture file! %s: %s",  System.lineSeparator(), stbi_failure_reason());
                return;
            }

            width = w.get();
            height = h.get();
        }
        createTexture(buffer);
    }

    public Texture(BufferedImage buffImg) {
        // essentially: BufferedImage to ByteBuffer
        width = buffImg.getWidth();
        height = buffImg.getHeight();

        // write pixels into an int array
        int[] pixels = new int[width * height];
        buffImg.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * BPP);  // 4 bytes per pixel
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = pixels[y * width + x];
                buffer.put((byte) ((pixel >> 16) & 0xFF));  // Red component
                buffer.put((byte) ((pixel >> 8) & 0xFF));   // Green component
                buffer.put((byte) (pixel & 0xFF));          // Blue component
                buffer.put((byte) ((pixel >> 24) & 0xFF));  // Alpha component
            }
        }
        createTexture(buffer);
    }

    private void createTexture(ByteBuffer buffer) {
        buffer.flip();  // flip to read mode for openGL

        texId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texId);
        setupTextureDefaults();

        // pass to openGL (internalFormat: format to be stored in, format: format of supplied image)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        unbind();

        MemoryUtil.memFree(buffer);  // may want to keep for later though :shrug:
    }

    private void setupTextureDefaults() {
        // pixel interpolation when scaling up or down
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // no wrap
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);  // GL_CLAMP_TO_EDGE?
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    public void bind() {bind(1);}
    public void bind(int slot) {
        if (boundSlots.contains(slot)) {
            Logging.warn("Overriding already set texture slot '%s'", slot);
            boundSlots.remove(slot);
        }

        glBindTextureUnit(slot, texId);
        boundSlots.add(slot);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}
