package src.rendering;

import modules.PNGDecoder.PNGDecoder;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL45;
import src.utility.Logging;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL45.*;

public class Texture {
    private int texId;

    int width;
    int height;
    int bpp = 4;  // bytes per pixel

    public Texture(String filePath) {
        ByteBuffer buffer;

        try (FileInputStream file = new FileInputStream(filePath)) {
            PNGDecoder decoder = new PNGDecoder(file);

            width = decoder.getWidth();
            height = decoder.getHeight();
            buffer = BufferUtils.createByteBuffer(bpp * width * height);
            decoder.decode(buffer, width * bpp, PNGDecoder.Format.RGBA);

            buffer.flip();  // flip to read mode for gl
        } catch (IOException ioe) {
            Logging.danger("PNG at location '%s' could not be loaded. Thrown message:\n%s", filePath, ioe);
            return;
        }

        texId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texId);
        setupTextureDefaults();

        // pass to openGL (internalFormat: format to be stored in, format: format of supplied image)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        unbind();

        buffer.clear();  // may want to keep for later though :shrug:
    }

    private void setupTextureDefaults() {
        // pixel interpolation when scaling up or down
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // no wrap
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);  // GL_CLAMP_TO_EDGE?
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
    }

    public void bind() {bind(0);}
    public void bind(int slot) {
        GL45.glActiveTexture(GL_TEXTURE0 + slot);
        glBindTexture(GL_TEXTURE_2D, texId);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }
}
