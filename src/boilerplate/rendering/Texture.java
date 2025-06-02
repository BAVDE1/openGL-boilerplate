package boilerplate.rendering;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import boilerplate.utility.Logging;

import javax.imageio.ImageIO;
import java.awt.*;
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

        public Image() {

        }

        public Image(ByteBuffer i, int w, int h) {
            buffer = i;
            width = w;
            height = h;
        }
    }

    protected static final Map<Integer, Integer> boundSlots = new HashMap<>();  // key: slot, value: texId
    /**
     * bytes per pixel
     */
    public static final int BPP = 4;
    /**
     * for debugging, if you wanted to write the texture to file to inspect it
     */
    public static File outputFile = new File("image.png");
    public int textureType = GL_UNSIGNED_BYTE;

    private Integer texId;
    public Dimension size = new Dimension();

    public Texture() {
    }

    public Texture(Dimension size, boolean generateId) {
        this.size = size;
        if (generateId) genId();
    }

    public Texture(String resourcePath) {
        createTextureFromImgBuff(resourcePathToBufferedImage(resourcePath));
    }

    public Texture(BufferedImage buffImg) {
        createTextureFromImgBuff(buffImg);
    }

    public void createTextureFromImgBuff(BufferedImage buffImg) {
        Image image = bufferedImageToByteImage(buffImg);
        size = new Dimension(image.width, image.height);
        createTextureFromByteBuff(image.buffer);
    }

    private void createTextureFromByteBuff(ByteBuffer buffer) {
        buffer.flip();  // flip to read mode for openGL

        genId();
        bind();
        useDefaultInterpolation();
        useDefaultWrap();
        createTexture(GL_RGBA, GL_RGBA, buffer);

        MemoryUtil.memFree(buffer);  // may want to keep for later though :shrug:
    }

    /**
     * internalFormat: format to be stored in
     * format: format of supplied image
     */
    public void createTexture(int storedFormat, int givenImgFormat, ByteBuffer buffer) {
        glTexImage2D(GL_TEXTURE_2D, 0, storedFormat, size.width, size.height, 0, givenImgFormat, textureType, buffer);
    }

    public int getId() {
        return texId;
    }

    public void genId() {
        if (texId != null) {
            Logging.warn("Attempting to re-generate already generated texture id, aborting");
            return;
        }
        texId = glGenTextures();
    }

    public void useDefaultInterpolation() {
        bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
    }

    public void setInterpolation(int interpolation) {
        bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, interpolation);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, interpolation);
    }

    public void useDefaultWrap() {
        bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    public void setWrap(int wrap) {
        bind();
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, wrap);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, wrap);
    }

    /**
     * meant for use with a texture array
     */
    public void bindToSlot(int slot) {
        if (slot == 0) {
            Logging.danger("cannot bind texture to slot 0, use anything > 0.");
            return;
        }

        if (boundSlots.containsKey(slot)) {
            Logging.warn("Overriding already set texture slot '%s'", slot);
            boundSlots.remove((Integer) slot);
        }

        glBindTextureUnit(slot, texId);
        boundSlots.put(slot, texId);
    }

    public void bindToTexArray(int slot, ShaderProgram sh) {
        bindToSlot(slot);
        sh.uniform1iv("textures", boundSlots.keySet().stream().mapToInt(i -> i).toArray());
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, texId);
    }

    public static void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void delete() {
        glDeleteTextures(texId);
    }

    public static Image loadImageFromFilePath(String resourcePath) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Prepare image buffers
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer buffer = stbi_load(resourcePath, w, h, comp, BPP);
            if (buffer == null) {
                Logging.danger("Failed to load a texture file! %s: %s", System.lineSeparator(), stbi_failure_reason());
                return null;
            }
            return new Image(buffer, w.get(), h.get());
        }
    }

    /**
     * debug function writes given image to src/image.png
     */
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

    public static BufferedImage resourcePathToBufferedImage(String resourcePath) {
        Logging.debug("Attempting to create buffered image from resource path: %s", resourcePath);
        URL url = ClassLoader.getSystemResource(resourcePath);
        if (url == null) {
            Logging.danger("Image failed to load from given filepath: '%s'", resourcePath);
            return null;
        }

        try {
            return ImageIO.read(url);
        } catch (IOException e) {
            Logging.danger("Image failed to load from given filepath: '%s'\nError:\n%s", resourcePath, e);
            return null;
        }
    }

    public static Image bufferedImageToByteImage(BufferedImage bufferedImage) {
        Image image = new Image();
        image.width = bufferedImage.getWidth();
        image.height = bufferedImage.getHeight();

        // get all pixels
        int[] pixels = new int[image.width * image.height];
        bufferedImage.getRGB(0, 0, image.width, image.height, pixels, 0, image.width);

        image.buffer = MemoryUtil.memAlloc(image.width * image.height * BPP);
        for (int y = 0; y < image.height; y++) {
            for (int x = 0; x < image.width; x++) {
                int pixel = pixels[y * image.width + x];
                image.buffer.put((byte) ((pixel >> 16) & 0xFF));  // Red component
                image.buffer.put((byte) ((pixel >> 8) & 0xFF));   // Green component
                image.buffer.put((byte) (pixel & 0xFF));          // Blue component
                image.buffer.put((byte) ((pixel >> 24) & 0xFF));  // Alpha component
            }
        }
        return image;
    }
}
