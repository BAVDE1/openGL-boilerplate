package src.rendering;

import modules.PNGDecoder.PNGDecoder;
import org.lwjgl.BufferUtils;
import src.utility.Logging;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;

public class Texture {
    private int texId;

    ByteBuffer imgBuffer;

    int width;
    int height;
    int bpp = 4;  // bytes per pixel

    public Texture(String filePath) {
        try (FileInputStream file = new FileInputStream(filePath)) {
            PNGDecoder decoder = new PNGDecoder(file);

            width = decoder.getWidth();
            height = decoder.getHeight();
            imgBuffer = BufferUtils.createByteBuffer(bpp * width * height);
            decoder.decodeFlipped(imgBuffer, width * bpp, PNGDecoder.Format.RGBA);  // flip it right way round lol

            imgBuffer.flip();  // flip to read mode for gl
        } catch (IOException ioe) {
            Logging.danger(String.format("PNG at location '%s' could not be loaded. Thrown message:\n%s", filePath, ioe));
            return;
        }

        texId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texId);
        setupTextureDefaults();

        // pass to openGL
        glTexImage2D();
    }

    private void setupTextureDefaults() {
        // pixel interpolation when scaling up or down
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        // no wrap
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP);  // GL_CLAMP_TO_EDGE?
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP);
    }

    public void bind() {bind(0);}
    public void bind(int slot) {
    }

    public void unbind() {

    }
}
