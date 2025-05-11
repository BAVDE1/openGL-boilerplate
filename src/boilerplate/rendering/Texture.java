package boilerplate.rendering;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import boilerplate.utility.Logging;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL45.*;
import static org.lwjgl.stb.STBImage.stbi_failure_reason;
import static org.lwjgl.stb.STBImage.stbi_load;

/**
 * Loads a texture from a file path or buffered image & binds to a slot
 */
public class Texture {
    public static class Image {
        public ByteBuffer buffer;
        public int width, height;

        Image(ByteBuffer i, int w, int h) {
            buffer = i;
            width = w;
            height = h;
        }
    }

    static int currentlyBoundTextureId = -1;
    static final Map<Integer, Integer> boundSlots = new HashMap<>();  // key: slot, value: texId
    /** bytes per pixel */
    static final int BPP = 4;
    /** for debugging, if you wanted to write the texture to file to inspect it */
    static File outputFile = new File("image.png");

    private int texId;
    public int width, height;

    public Texture(String resourcePath) {
        Logging.debug("Attempting to create texture from resource path: %s", resourcePath);
        URL url = ClassLoader.getSystemResource(resourcePath);
        if (url == null) {
            Logging.danger("Image failed to load from given filepath: '%s'", resourcePath);
            return;
        }

        try {
            createTexture(ImageIO.read(url));
        } catch (IOException e) {
            Logging.danger("Image failed to load from given filepath: '%s'\nError:\n%s", resourcePath, e);
        }
    }

    public Texture(BufferedImage buffImg) {
        createTexture(buffImg);
    }

    /** essentially BufferedImage to ByteBuffer */
    public void createTexture(BufferedImage buffImg) {
        width = buffImg.getWidth();
        height = buffImg.getHeight();

        // get all pixels
        int[] pixels = new int[width * height];
        buffImg.getRGB(0, 0, width, height, pixels, 0, width);

        ByteBuffer buffer = MemoryUtil.memAlloc(width * height * BPP);
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
        setDefaultInterpolation();
        setDefaultWrap();

        // internalFormat: format to be stored in
        // format: format of supplied image
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        unbind();

        MemoryUtil.memFree(buffer);  // may want to keep for later though :shrug:
        Logging.debug("Texture created, texId: %s", texId);
    }

    public void setDefaultInterpolation() {
        bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }

    public void setInterpolation(int interpolation) {
        bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, interpolation);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, interpolation);
    }

    public void setDefaultWrap() {
        bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    public void setWrap(int wrap) {
        bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrap);
    }

    public static Image loadImageFromFilePath(String resourcePath) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Prepare image buffers
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer buffer = stbi_load(resourcePath, w, h, comp, BPP);
            if (buffer == null) {
                Logging.danger("Failed to load a texture file! %s: %s",  System.lineSeparator(), stbi_failure_reason());
                return null;
            }
            return new Image(buffer, w.get(), h.get());
        }
    }

    public void bind() {
        if (currentlyBoundTextureId == texId) return;
        currentlyBoundTextureId = texId;
        glBindTexture(GL_TEXTURE_2D, texId);
    }

    /** meant for use with a texture array */
    public void bindToSlot(int slot) {
        if (slot == 0) {
            Logging.danger("cannot bind texture to slot 0, use anything > 0.");
            return;
        }

        if (boundSlots.containsKey(slot)) {
            Logging.warn("Overriding already set texture slot '%s'", slot);
            boundSlots.remove((Integer) slot);
        }

        glBindTextureUnit(GL_TEXTURE_2D, texId);
        boundSlots.put(slot, texId);
    }

    public void bindToTexArray(int slot, ShaderHelper sh) {
        bindToSlot(slot);
        sh.uniform1iv("textures", boundSlots.keySet().stream().mapToInt(i -> i).toArray());
    }

    public static void unbind() {
        currentlyBoundTextureId = -1;
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void delete() {
        if (currentlyBoundTextureId == texId) unbind();
        glDeleteTextures(texId);
    }

    /** debug function writes given image to src/image.png */
    public static void writeToFile(BufferedImage img) {
        try {
            ImageIO.write(img, "png", outputFile);
        } catch (IOException e) {
            Logging.danger("Failed to write given image to file 'src/image.png'\nError message thrown:\n%s", e);
        }
    }

    public static void deleteAll() {
        unbind();
        for (int id : boundSlots.values()) glDeleteTextures(id);
        boundSlots.clear();
    }
}
