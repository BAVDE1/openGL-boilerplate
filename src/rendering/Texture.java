package rendering;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import utility.Logging;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Comparator;

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

    static final ArrayList<Integer> boundSlots = new ArrayList<>();
    static final int BPP = 4;  // bytes per pixel

    private int texId;
    public int width, height;

    public Texture(String filePath) {
        Image image = loadImageFromFilePath(filePath);
        if (image == null) {
            Logging.danger("Image failed to load from given filepath: '%s'", filePath);
            return;
        }

        width = image.width;
        height = image.height;
        createTexture(image.buffer);
    }

    public Texture(BufferedImage buffImg) {
        // essentially: BufferedImage to ByteBuffer
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
        setupTextureDefaults();

        // internalFormat: format to be stored in
        // format: format of supplied image
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        unbind();

        MemoryUtil.memFree(buffer);  // may want to keep for later though :shrug:
    }

    private void setupTextureDefaults() {
        // pixel interpolation when scaling up or down
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // no wrap
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    public static Image loadImageFromFilePath(String filePath) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Prepare image buffers
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer comp = stack.mallocInt(1);

            ByteBuffer buffer = stbi_load(filePath, w, h, comp, BPP);
            if (buffer == null) {
                Logging.danger("Failed to load a texture file! %s: %s",  System.lineSeparator(), stbi_failure_reason());
                return null;
            }
            return new Image(buffer, w.get(), h.get());
        }
    }

    public void bind(int slot) {
        if (slot == 0) {
            Logging.danger("cannot bind texture to slot 0, use anything > 0");
            return;
        }

        if (boundSlots.contains(slot)) {
            Logging.warn("Overriding already set texture slot '%s'", slot);
            boundSlots.remove((Integer) slot);
        }

        glBindTextureUnit(slot, texId);
        boundSlots.add(slot);
        boundSlots.sort(Comparator.naturalOrder());
    }

    public void bindToTexArray(int slot, ShaderHelper sh) {
        bind(slot);
        ShaderHelper.uniform1iv(sh, "textures", boundSlots.stream().mapToInt(i -> i).toArray());
    }

    public static void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    /** debug function writes given image to src/image.png */
    public static void writeToFile(BufferedImage img) {
        File outputfile = new File("image.png");
        try {
            ImageIO.write(img, "png", outputfile);
        } catch (IOException e) {
            Logging.danger("Failed to write given image to file 'src/image.png'\nError message thrown:\n%s", e);
        }
    }
}
